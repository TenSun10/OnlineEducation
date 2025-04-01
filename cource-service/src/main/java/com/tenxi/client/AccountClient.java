package com.tenxi.client;

import com.tenxi.config.FeignClientConfig;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.utils.RestBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 指定配置文件（配置文件中添加了自定义拦截器）
 */
@FeignClient(name = "OE-user-service", configuration = FeignClientConfig.class)
public interface AccountClient {

    @GetMapping("/users/{id}")
    RestBean<AccountDetailVo> queryAccountById(@PathVariable Long id);
}
