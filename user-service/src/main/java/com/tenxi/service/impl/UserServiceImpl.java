package com.tenxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.entity.dto.PasswordResetDto;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.utils.BaseContext;
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

import static com.tenxi.utils.ConstStr.ASK_CODE_REQUEST_LIMIT;
import static com.tenxi.utils.ConstStr.VERIFY_CODE;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Account> implements UserService {
    @Resource
    private MailListener mailListener;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserMapper userMapper;

    /**
     * 获取验证码 一分钟请求间隔
     * @param email
     * @param type
     * @return
     */
    @Override
    public RestBean<String> askCode(String email, String type) {
        //1.请求频率检查
        String limitKey = ASK_CODE_REQUEST_LIMIT + email;
        String lastRequestTime = stringRedisTemplate.opsForValue().get(limitKey);

        if (StringUtils.hasText(lastRequestTime)) {
            long lastTime = Long.parseLong(lastRequestTime);
            long currentTime = System.currentTimeMillis();
            // 1分钟内只能请求一次
            if (currentTime - lastTime < 60 * 1000) {
                long waitTime = 60 - (currentTime - lastTime) / 1000;
                return RestBean.failure(1006, "请求过于频繁，请" + waitTime + "秒后再试");
            }
        }

        synchronized (email.intern()) {
            try {
                // 2. 记录本次请求时间
                stringRedisTemplate.opsForValue().set(limitKey,
                        String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);

                // 4. 发送验证码业务逻辑
                Random random = new Random();
                int code = random.nextInt(899999) + 100000;
                setRedisCode(email, String.valueOf(code));

                Map<String, Object> data = new HashMap<>();
                data.put("email", email);
                data.put("type", type);
                data.put("code", String.valueOf(code));
                mailListener.sendMail(data);

                return RestBean.successWithMsg("验证码获取成功");

            } catch (Exception e) {
                // 如果发送失败，清除限制记录，允许用户重试
                stringRedisTemplate.delete(limitKey);
                throw e;
            }
        }
    }

    /**
     * 用户注册
     * @param vo
     * @return
     */
    @Override
    public RestBean<String> register(EmailRegisterDto vo) {
        //先获取用户传递的code和redis中存储的code是否一致
        String voCode = vo.getCode();
        if(!StringUtils.hasText(voCode)) {
            throw new BusinessException(ErrorCode.CODE_IS_NULL);
        }
        String redisCode = getRedisCode(vo.getEmail());
        if(!voCode.equals(redisCode)) {
            throw new BusinessException(ErrorCode.CODE_VALID_FAILED);
        }

        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Account::getEmail, vo.getEmail());
        Account accountTest = getOne(queryWrapper);
        if(accountTest != null) {
            throw new BusinessException(ErrorCode.USER_RESET_ERROR);
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
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        AccountDetailVo vo = new AccountDetailVo(account.getId(), account.getEmail(), account.getUsername(),account.getAvatar(), account.getRegisterTime());
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
     * 重新设置密码
     * @param dto
     * @return
     */
    @Override
    public RestBean<String> resetPassword(PasswordResetDto dto) {
        String code = getRedisCode(dto.getEmail());
        if(!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.CODE_IS_NULL);
        }
        if(!code.equals(dto.getCode())) {
            throw new BusinessException(ErrorCode.CODE_VALID_FAILED);
        }
        Long userId = BaseContext.getCurrentId();

        int result = userMapper.setNewPassword(userId, dto.getEmail(), dto.getNewPassword());

        if (result != 1) {
            throw new BusinessException(ErrorCode.SERVER_INNER_ERROR);
        }
        return RestBean.successWithMsg("重新设置密码成功");
    }

    /**
     * 用于Feign之间调用的批量查询操作
     * 不需要管理员权限
     * @param userIds
     * @return
     */
    @Override
    public RestBean<List<AccountDetailVo>> getBatchAccount(Set<Long> userIds) {
        return batchUsers(new ArrayList<>(userIds));
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
