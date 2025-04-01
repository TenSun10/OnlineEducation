package com.tenxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxi.entity.RedisData;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CategoryAddDTO;
import com.tenxi.entity.po.Category;

import com.tenxi.entity.vo.CategoryTreeVO;
import com.tenxi.mapper.CategoryMapper;

import com.tenxi.service.CategoryService;

import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static com.tenxi.common.CategoryConstStr.CACHE_CATEGORY;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public String addCategory(CategoryAddDTO dto) {
        Category category = new Category(dto.getLabel(), dto.getParentId(), dto.getLevel(), dto.getOrderNum(), dto.getStatus(), LocalDateTime.now());
        Category entity = lambdaQuery().eq(Category::getLabel, category.getLabel()).one();
        if (Objects.nonNull(entity)) {
            return "该分类已存在";
        }

        boolean save = save(category);
        //出现更改则清除redis缓存
        if(save){
            redisTemplate.delete(CACHE_CATEGORY);
            return null;
        }else return "上传失败, 请重试";
    }


    /**
     * 考虑到分类的更新一般属于不经常性的,所以还是直接在redis存储转换好的树形结构
     * 补充:作为热点数据需要防止缓存击穿
     * @return
     */
    @Override
    public RestBean<List<CategoryTreeVO>> buildCategoryTree() {
        //1.先尝试从redis中获取缓存
        Object cacheTree = redisTemplate.opsForValue().get(CACHE_CATEGORY);
        List<CategoryTreeVO> res = null;
        if (!Objects.isNull(cacheTree)) {
            //将Object转换为CategoryTreeVO
            try {
                //当缓存List<CategoryTreeVO>时，Redis中存储的实际类型是List<LinkedHashMap>，需要借助convertValue方法转换
                RedisData<List<CategoryTreeVO>> redisData = objectMapper.convertValue(cacheTree, new TypeReference<RedisData>() {});
                //判断是否逻辑过期
                if (redisData != null && redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                    res = redisData.getData();
                    return RestBean.successWithData(res);
                }
            }catch (IllegalArgumentException e) {
                //如果类型转换失败就清楚脏数据
                redisTemplate.delete(CACHE_CATEGORY);
                return null;
            }
        }

        //过期了就获取锁,成功则进行缓存重建
        if(tryLock()) {
           try {
               //开启独立线程处理缓存重建
               CACHE_REBUILD_EXECUTOR.submit(this::saveCategory2Redis);
           }finally {
               unlock();
           }
        }

        return RestBean.successWithData(res);
    }


    /**
     * 修改分类的状态
     * @param id
     * @return
     */
    @Override
    public String changeStatus(Long id) {

        Category byId = getById(id);
        if (byId.getStatus() == 0) byId.setStatus(1);
        else byId.setStatus(0);

        boolean update = updateById(byId);

        //出现更改则清除redis缓存
        if(update) {
            redisTemplate.delete(CACHE_CATEGORY);
            return null;
        }else return "修改失败, 请重试";
    }

    private CategoryTreeVO convertToTree(Category root, List<Category> all) {
        //使用BeanUtil赋值
        CategoryTreeVO node = new CategoryTreeVO();
        BeanUtils.copyProperties(root, node);

        //递归获取下层
        List<CategoryTreeVO> children = all.stream()
                .filter(c -> c.getParentId().equals(root.getId()))
                //获取下一层
                .map(c -> convertToTree(c, all))
                .sorted(Comparator.comparingInt(CategoryTreeVO::getOrderNum))
                .toList();

        node.setChildren(children);
        return node;
    }

    /**
     * 尝试获取锁
     *
     * @return
     */
    private boolean tryLock() {
        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent("online:category:tree_lock", "1", 20, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(b);
    }

    /**
     * 释放锁
     */
    private void unlock() {
        stringRedisTemplate.delete("online:category:tree_lock");
    }

    /**
     * 封装分类缓存重建方法
     * @return
     */
    private void saveCategory2Redis() {
        // 1. 查询所有有效分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getOrderNum);
        wrapper.eq(Category::getStatus, 1);
        List<Category> allCategories = categoryMapper.selectList(wrapper);

        if (allCategories.isEmpty()) {
            //设置更短的过期时间
            redisTemplate.opsForValue().set(CACHE_CATEGORY, allCategories, 1, TimeUnit.MINUTES);
        }
        //2.给每一个最高层节点生成一个树型结构
        List<CategoryTreeVO> res = allCategories.stream()
                .filter(c -> c.getParentId().equals(0L))
                .map(root -> convertToTree(root, allCategories))
                .sorted(Comparator.comparingInt(CategoryTreeVO::getOrderNum))
                .toList();

        //3.转化为RedisData(逻辑过期时间2天)
        RedisData redisData = new RedisData(LocalDateTime.now().plusDays(2), res);

        //4.存入redis
        redisTemplate.opsForValue().set(CACHE_CATEGORY, redisData, 10, TimeUnit.DAYS);
    }
}
