package com.tenxi.pay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用支付宝支付的配置类
 */
@Configuration
public class AlipayConfig {
    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")
    private String privateKey;

    @Getter
    @Value("${alipay.public-key}")
    private String alipayPublicKey;

    @Value("${alipay.gateway}")
    private String gateway;

    @Getter
    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gateway,
                appId,
                privateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );
    }
}
