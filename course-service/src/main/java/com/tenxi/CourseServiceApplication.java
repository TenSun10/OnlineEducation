package com.tenxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 扫描其他模块的时候是不会自动把其中的beans注册到spring容器中的
 * 所以需要我们自告诉spring boot应该扫描到哪些包
 */

@EnableScheduling
@EnableFeignClients(basePackages = "com.tenxi.client")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootApplication(scanBasePackages = {"com.tenxi"})
public class
CourseServiceApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(CourseServiceApplication.class, args);
    }
}
