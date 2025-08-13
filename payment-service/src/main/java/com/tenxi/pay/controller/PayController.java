package com.tenxi.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.pay.config.AlipayConfig;
import com.tenxi.pay.entity.dto.PayCreateDTO;
import com.tenxi.pay.enums.PayStatus;
import com.tenxi.pay.service.PayService;
import com.tenxi.utils.RestBean;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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


    @Operation(
            summary = "订单支付操作",
            description = "用户对创建的订单进行支付，返回给前端支付链接",
            responses = {
                    @ApiResponse(responseCode = "503", description = "订单服务不可用"),
                    @ApiResponse(responseCode = "403", description = "未知错误请联系管理员"),
                    @ApiResponse(responseCode = "405", description = "订单已超时，需要重新创建")
            }
    )
    @PostMapping("/create")
    public RestBean<String> createPayOrder(@RequestBody PayCreateDTO dto) throws AlipayApiException {
        return payService.createPayOrder(dto);
    }


    /**
     * 处理支付宝异步回调
     * @param request
     * @return
     */
    @Hidden
    @PostMapping("/callback/alipay")
    public String handleAlipayCallback(HttpServletRequest request) {
        return payService.handlerAlipayCallback(request);
    }



}
