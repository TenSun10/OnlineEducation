package com.tenxi.controller;

import com.tenxi.entity.dto.PasswordResetDto;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.EmailRegisterDto;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Log
@Tag(name = "用户登录注册", description = "用户登录注册等相关接口")
@RestController
@RequestMapping("/users")

public class UserController {
    @Resource
    private ControllerHandler controllerHandler;
    @Resource
    private UserService userService;


    @Operation(
            summary = "获取验证码",
            description = "用户通过邮箱获取验证码，使用RabbitMQ异步获取"
    )
    @RequestMapping("/ask-code")
    public RestBean<String> askCode(@RequestParam String email,
                                    @RequestParam String type) {
        return controllerHandler.messageHandler(() ->
            userService.askCode(email, type));
    }

    @Operation(
            summary = "账号注册",
            description = "用户通过获取到的验证码，输入自己的信息注册账号"
    )
    @PostMapping("/register")
    public RestBean<String> register(@RequestBody EmailRegisterDto emailRegisterDto) {
        return controllerHandler.messageHandler(() ->
                userService.regitser(emailRegisterDto));
    }

    @Operation(
            summary = "根据id查询用户信息",
            description = "url拼接的id查询用户可对外开放信息"
    )
    @GetMapping("/{id}")
    public RestBean<AccountDetailVo> getAccount(@PathVariable Long id) {
        return userService.getAccount(id);
    }

    @Operation(
            summary = "查询所有的用户信息",
            description = "管理员查询所有用户的可对外开放信息"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/batch")
    RestBean<List<AccountDetailVo>> batchQueryAccounts(@RequestBody List<Long> userIds) {
        return userService.batchUsers(userIds);
    }

    @Operation(
            summary = "重置账号密码",
            description = "用户通过获取到的验证码，输入自己的旧密码和新密码"
    )
    @PostMapping("/reset-password")
    public RestBean<String> resetPassword(@RequestBody PasswordResetDto dto) {
        return controllerHandler.messageHandler(() ->
                userService.resetPassword(dto));
    }

    @Operation(
            summary = "获取对应的ids的用户信息",
            description = "批量查询用户信息"
    )
    @PostMapping("/batch")
    public RestBean<List<AccountDetailVo>> getBatchAccounts(@RequestBody Set<Long> userIds) {
        return userService.getBatchAccount(userIds);
    }
}
