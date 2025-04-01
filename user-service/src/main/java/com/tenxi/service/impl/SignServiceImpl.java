package com.tenxi.service.impl;

import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import com.tenxi.service.SignService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户打卡的功能实现
 */
@Log
@Service
public class SignServiceImpl implements SignService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 打卡操作
     * 利用redis里面的BitMap实现这一功能
     * @return
     */
    @Override
    public String sign() {
        String key = getRedisKey();
        log.info("用户" + key + "正在打卡");

        int day = LocalDate.now().getDayOfMonth();

        Boolean b = stringRedisTemplate.opsForValue().setBit(key, day - 1, true);
        if (Boolean.TRUE.equals(b)) {
            return null;
        }
        return "打卡失败, 请重试";
    }

    /**
     * 获取今日的打卡状态
     * @return
     */
    @Override
    public RestBean<Boolean> getTodayStatus() {
        String key = getRedisKey();
        log.info("用户" + key + "正在查询打卡状态");
        int day = LocalDate.now().getDayOfMonth();

        Boolean bit = stringRedisTemplate.opsForValue().getBit(key, day - 1);

        if (bit == null) {
            return RestBean.successWithData(false);
        }
        return RestBean.successWithData(bit);
    }

    @Override
    public RestBean<Integer> getCount() {
        String key = getRedisKey();
        log.info("用户" + key + "正在查询连续打卡天数");
        int day = LocalDate.now().getDayOfMonth();

        List<Long> longs = stringRedisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(day)).valueAt(0));

        if(longs == null || longs.isEmpty()) {
            return RestBean.successWithData(0);
        }

        Long num = longs.get(0);
        int cnt = 0;
        while ((num & 1) == 1) {
            cnt++;
            num >>>= 1;
        }

        return RestBean.successWithData(cnt);
    }


    public String getRedisKey() {
        Long id = BaseContext.getCurrentId();
        LocalDate now = LocalDate.now();
        return now.getYear() + ":" + now.getMonthValue() + ":" + id;
    }
}
