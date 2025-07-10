package com.tenxi.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<Account> {
}