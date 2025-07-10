package com.tenxi.notification.client;

import com.tenxi.config.FeignClientConfig;
import com.tenxi.notification.entity.vo.AccountDetailVo;
import com.tenxi.utils.RestBean;
import feign.form.FormData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@FeignClient(
        name = "OE-user-service",
        configuration = {FeignClientConfig.class}
)
public interface AccountClient {
    @GetMapping({"/users/{id}"})
    RestBean<AccountDetailVo> queryAccountById(@PathVariable Long id);

    @PostMapping("/users/batch")
    RestBean<List<AccountDetailVo>> batchQueryAccounts(@RequestBody Set<Long> userIds);

}
