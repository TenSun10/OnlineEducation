package com.tenxi.pay.client;

import com.tenxi.config.FeignClientConfig;
import com.tenxi.pay.entity.vo.OrderVO;
import com.tenxi.utils.RestBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "OE-order-service",
        contextId = "paymentOrderClient",
        configuration = FeignClientConfig.class)
public interface OrderClient {
    @GetMapping("/{id}")
    RestBean<OrderVO> getOrder(@PathVariable("id") Long id);
    @GetMapping("/status/{id}")
    RestBean<String> changeOrderStatus(@PathVariable("id") Long id);
}
