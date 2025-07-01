package com.tenxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.entity.dto.PasswordResetDto;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.Account;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.entity.dto.EmailRegisterDto;
import com.tenxi.listener.MailListener;
import com.tenxi.mapper.UserMapper;
import com.tenxi.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.tenxi.utils.ConstStr.VERIFY_CODE;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Account> implements UserService {
    @Resource
    private MailListener mailListener;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 获取验证码
     * @param email
     * @param type
     * @return
     */
    @Override
    public String askCode(String email, String type) {
        synchronized (email.intern()) {
            //生成验证码
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            //将验证码存入redis,设置过期时间
            setRedisCode(email, String.valueOf(code));
            //利用mq将信息异步发送
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("type", type);
            data.put("code", String.valueOf(code));
            mailListener.sendMail(data);

            return null;
        }
    }

    /**
     * 用户注册
     * @param vo
     * @return
     */
    @Override
    public String regitser(EmailRegisterDto vo) {
        //先获取用户传递的code和redis中存储的code是否一致
        String voCode = vo.getCode();
        if(!StringUtils.hasText(voCode)) {
            return "验证码为空,请先获取验证码";
        }
        String redisCode = getRedisCode(vo.getEmail());
        if(!voCode.equals(redisCode)) {
            return "验证码错误,请重新输入";
        }

        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Account::getEmail, vo.getEmail());
        Account accountTest = getOne(queryWrapper);
        if(accountTest != null) {
            return "当前用户已存在,请勿重复注册";
        }

        Account account = new Account(vo.getEmail(), passwordEncoder.encode(vo.getPassword()), vo.getRole(), LocalDateTime.now());
        save(account);
        deleteVerifyCode(vo.getEmail());
        return null;
    }

    /**
     * 根据用户id查询用户的可公布信息
     * @param id
     * @return
     */
    @Override
    public RestBean<AccountDetailVo> getAccount(Long id) {
        Account account = getById(id);
        if(account == null) {
            return RestBean.successWithMsg("为查询到该用户的信息");
        }
        AccountDetailVo vo = new AccountDetailVo(account.getId(), account.getEmail(), account.getAvatar(), account.getRegisterTime());
        return RestBean.successWithData(vo);
    }

    /**
     * 根据批量id查询用户
     * @param userIds
     * @return
     */
    @Override
    public RestBean<List<AccountDetailVo>> batchUsers(List<Long> userIds) {
        List<Account> accounts = listByIds(userIds);
        List<AccountDetailVo> vos = transToVo(accounts);
        return RestBean.successWithData(vos);
    }

    /**
     * TODO 重新设置密码
     * @param dto
     * @return
     */
    @Override
    public String resetPassword(PasswordResetDto dto) {
        return "";
    }


    private List<AccountDetailVo> transToVo(List<Account> accounts) {
        if(accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        return accounts.stream().map(item -> {
            AccountDetailVo vo = new AccountDetailVo();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
    }


    public void setRedisCode(String email, String code) {
        stringRedisTemplate.opsForValue().set(VERIFY_CODE + email, code, 3, TimeUnit.MINUTES);
    }

    public String getRedisCode(String email) {
        return stringRedisTemplate.opsForValue().get(VERIFY_CODE + email);
    }

    /**
     * 将从redis中删除验证码操作封装
     * @param email
     */
    public void deleteVerifyCode(String email) {
        stringRedisTemplate.delete(VERIFY_CODE + email);
    }
}
