package com.tenxi.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.pay.config.AlipayConfig;
import com.tenxi.pay.entity.dto.PayCreateDTO;
import com.tenxi.pay.enums.PayStatus;
import com.tenxi.pay.service.PayService;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Resource
    private PayService payService;
    @Resource
    private ControllerHandler controllerHandler;


    @PostMapping("/create")
    public RestBean<String> createPayOrder(@RequestBody PayCreateDTO dto) throws AlipayApiException {
        return payService.createPayOrder(dto);
    }


    /**
     * 处理支付宝异步回调
     * @param request
     * @return
     */
    @PostMapping("/callback/alipay")
    public String handleAlipayCallback(HttpServletRequest request) {
        return payService.handlerAlipayCallback(request);
    }



}
