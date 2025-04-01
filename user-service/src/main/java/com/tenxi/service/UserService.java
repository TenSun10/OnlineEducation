package com.tenxi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.Account;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.entity.dto.EmailRegisterDto;

public interface UserService extends IService<Account> {
    String askCode(String email, String type);

    String regitser(EmailRegisterDto emailRegisterDto);

    RestBean<AccountDetailVo> getAccount(Long id);

}
