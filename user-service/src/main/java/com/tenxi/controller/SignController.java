package com.tenxi.controller;

import com.tenxi.utils.RestBean;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户打卡相关的接口
 */
@RestController
@Tag(name = "用户打卡管理", description = "用户打卡相关的API接口")
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
    @Operation(
            summary = "用户打卡",
            description = "用户进行每日打卡操作，使用redis bitmap存储",
            responses = {
                    @ApiResponse(responseCode = "200", description = "打卡成功"),
                    @ApiResponse(responseCode = "400", description = "重复打卡或打卡失败")
            }
    )
    @GetMapping
    public RestBean<String> sign() {
        return controllerHandler.messageHandler(() ->
                signService.sign());
    }

    /**
     * 获取今日打卡状态
     * @return
     */
    @Operation(
            summary = "获取今日打卡状态",
            description = "查询当前用户今天是否已经打卡"
    )
    @GetMapping("/status")
    public RestBean<Boolean> getStatus() {
        return signService.getTodayStatus();
    }

    /**
     * 获取连续打卡天数
     * @return
     */
    @Operation(
            summary = "获取连续打卡天数",
            description = "统计用户当前连续打卡的天数"
    )
    @GetMapping("/count")
    public RestBean<Integer> getCount() {
        return signService.getCount();
    }
}
