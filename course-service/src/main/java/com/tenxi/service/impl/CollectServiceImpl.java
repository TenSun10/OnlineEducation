package com.tenxi.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.entity.po.Collect;
import com.tenxi.entity.vo.CourseVO;
import com.tenxi.mapper.CollectMapper;
import com.tenxi.service.CollectService;
import com.tenxi.service.CourseService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.tenxi.common.CourseConstStr.CACHE_COURSE_COLLECT_HASH;
import static com.tenxi.common.CourseConstStr.CACHE_COURSE_COLLECT_SET;

@Slf4j
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CollectMapper collectMapper;

    @Resource
    private CourseService courseService;


    @Resource
    private DefaultRedisScript<List> atomicSyncScript;


    @Override
    public String userCollectCourse(Long id) {
        Long currentId = BaseContext.getCurrentId();
        String key_hash = CACHE_COURSE_COLLECT_HASH + currentId;

        //1.修改用户操作的最后 获取当前操作类型（取反）
        String lastAction = (String) redisTemplate.opsForHash().get(key_hash, id.toString());

        int newAction = 0;

        //2.如果在redis中未找到相应的key 就查数据库
        if (lastAction == null) {
            LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Collect::getCourseId, id);
            queryWrapper.eq(Collect::getUserId, currentId);

            Collect one = getOne(queryWrapper);
            if (one == null) {
                newAction = 1;
            }
        }else {
            newAction = Integer.parseInt(lastAction) ^ 1;
        }

        //记录最新操作
        redisTemplate.opsForHash().put(key_hash, id.toString(), String.valueOf(newAction));
        redisTemplate.expire(key_hash, 1, TimeUnit.HOURS);


        return null;
    }

    @Override
    public RestBean<List<CourseVO>> getAllCollectCourse() {
        Long userId = BaseContext.getCurrentId();

        List<Long> courseIds = collectMapper.getCourseIdsByUserId(userId);

        List<CourseVO> courseVOS = courseService.getBatchCourses(courseIds);

        return RestBean.successWithData(courseVOS);
    }


    /**
     * 定时任务 处理redis中课程收藏情况 存入或者删除相应的数据库数据
     * 使用lua脚本实现取出删除原子性
     */
    @Scheduled(fixedDelay = 6000)
    public void synCollectOperation() {
        log.info("收藏定时任务处理了......");
        Set<String> keys = redisTemplate.keys(CACHE_COURSE_COLLECT_HASH+"*");
        if(keys == null) return;

        for (String key : keys) {
            List operations = redisTemplate.execute(
                    atomicSyncScript,
                    Collections.singletonList(key)
            );

            if (operations == null || operations.isEmpty()) continue;

            Long userId = Long.parseLong(key.substring(CACHE_COURSE_COLLECT_HASH.length()));
            processUserOperations(userId, operations);
        }
    }

    private void processUserOperations(Long userId, List<Object> operations) {
        List<Long> toAdd = new ArrayList<>();
        List<Long> toRemove = new ArrayList<>();

        for (int i = 0; i < operations.size(); i += 2) {
            Long id = Long.parseLong(operations.get(i).toString());
            String action = operations.get(i + 1).toString();

            if ("1".equals(action)) toAdd.add(id);
            else toRemove.add(id);
        }

        if (!toAdd.isEmpty()) {
            // 生成ID列表
            List<Long> ids = toAdd.stream()
                    .map(courseId -> IdWorker.getId()) // 使用MyBatis-Plus的ID生成器
                    .collect(Collectors.toList());

            collectMapper.batchInsertIgnore(userId, toAdd, LocalDateTime.now(), ids);
        }

        if (!toRemove.isEmpty()) {
            LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Collect::getUserId, userId);
            queryWrapper.in(Collect::getCourseId, toRemove);
            collectMapper.delete(queryWrapper);
        }
    }
}