package com.tenxi.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tenxi.entity.Account;
import com.tenxi.entity.LoginUser;
import com.tenxi.notification.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements UserDetailsService {
    @Resource
    private UserMapper userMapper;

    public LoginServiceImpl() {
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Account::getEmail, username);
        Account account = (Account)this.userMapper.selectOne(queryWrapper);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        } else {
            return new LoginUser(account);
        }
    }
}