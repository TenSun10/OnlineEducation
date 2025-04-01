package com.tenxi.pay.service;

import com.alipay.api.AlipayApiException;
import com.tenxi.pay.entity.dto.PayCreateDTO;
import com.tenxi.pay.enums.PayStatus;
import com.tenxi.utils.RestBean;
import jakarta.servlet.http.HttpServletRequest;

public interface PayService {
    RestBean<String> createPayOrder(PayCreateDTO dto) throws AlipayApiException;

    String handlerAlipayCallback(HttpServletRequest request);
}
