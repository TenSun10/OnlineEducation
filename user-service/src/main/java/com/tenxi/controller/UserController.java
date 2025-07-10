package com.tenxi.controller;

import com.tenxi.entity.dto.PasswordResetDto;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.EmailRegisterDto;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Log
@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private ControllerHandler controllerHandler;
    @Resource
    private UserService userService;


    @RequestMapping("/ask-code")
    public RestBean<String> askCode(@RequestParam String email,
                                    @RequestParam String type) {
        return controllerHandler.messageHandler(() ->
            userService.askCode(email, type));
    }

    @PostMapping("/register")
    public RestBean<String> register(@RequestBody EmailRegisterDto emailRegisterDto) {
        return controllerHandler.messageHandler(() ->
                userService.regitser(emailRegisterDto));
    }

    @GetMapping("/{id}")
    public RestBean<AccountDetailVo> getAccount(@PathVariable Long id) {
        return userService.getAccount(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/batch")
    RestBean<List<AccountDetailVo>> batchQueryAccounts(@RequestBody List<Long> userIds) {
        return userService.batchUsers(userIds);
    }

    @PostMapping("/reset-password")
    public RestBean<String> resetPassword(@RequestBody PasswordResetDto dto) {
        return controllerHandler.messageHandler(() ->
                userService.resetPassword(dto));
    }

    @PostMapping("/batch")
    public RestBean<List<AccountDetailVo>> getBatchAccounts(@RequestBody Set<Long> userIds) {
        return userService.getBatchAccount(userIds);
    }
}
