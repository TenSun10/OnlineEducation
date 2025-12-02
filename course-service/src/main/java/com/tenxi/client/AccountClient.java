package com.tenxi.client;

import com.tenxi.config.FeignClientConfig;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.utils.RestBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 指定配置文件（配置文件中添加了自定义拦截器）
 */
@FeignClient(name = "OE-user-service",
        contextId = "courseAccountClient",
        configuration = FeignClientConfig.class)
public interface AccountClient {

    @GetMapping("/users/{id}")
    RestBean<AccountDetailVo> queryAccountById(@PathVariable("id") Long id);

    @PostMapping("/users/batch")
    RestBean<List<AccountDetailVo>> batchQueryAccounts(@RequestBody List<Long> userIds);
}
