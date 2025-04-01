package com.tenxi.controller;

import com.tenxi.utils.RestBean;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.SignService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户打卡相关的接口
 */
@RestController
@RequestMapping("/sign")
public class SignController {
    @Resource
    private ControllerHandler controllerHandler;
    @Resource
    private SignService signService;

    /**
     * 打卡
     * @return
     */
    @GetMapping
    public RestBean<String> sign() {
        return controllerHandler.messageHandler(() ->
                signService.sign());
    }

    /**
     * 获取今日打卡状态
     * @return
     */
    @GetMapping("/status")
    public RestBean<Boolean> getStatus() {
        return signService.getTodayStatus();
    }

    /**
     * 获取连续打卡天数
     * @return
     */
    @GetMapping("/count")
    public RestBean<Integer> getCount() {
        return signService.getCount();
    }
}
