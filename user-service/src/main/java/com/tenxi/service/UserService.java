package com.tenxi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.entity.dto.PasswordResetDto;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.Account;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.entity.dto.EmailRegisterDto;

import java.util.List;
import java.util.Set;

public interface UserService extends IService<Account> {
    String askCode(String email, String type);

    String regitser(EmailRegisterDto emailRegisterDto);

    RestBean<AccountDetailVo> getAccount(Long id);

    RestBean<List<AccountDetailVo>> batchUsers(List<Long> userIds);

    String resetPassword(PasswordResetDto dto);

    RestBean<List<AccountDetailVo>> getBatchAccount(Set<Long> userIds);
}
