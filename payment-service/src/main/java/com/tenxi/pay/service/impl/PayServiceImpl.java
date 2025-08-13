package com.tenxi.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.primitives.Longs;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.pay.client.OrderClient;
import com.tenxi.pay.config.AlipayConfig;
import com.tenxi.pay.entity.dto.PayCreateDTO;
import com.tenxi.pay.entity.po.OrderDetail;
import com.tenxi.pay.entity.vo.OrderVO;
import com.tenxi.pay.enums.PayStatus;
import com.tenxi.pay.mapper.PayMapper;
import com.tenxi.pay.service.PayService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import feign.FeignException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tenxi.pay.enums.PayStatus.PENDING;

@Slf4j
@Service
public class PayServiceImpl extends ServiceImpl<PayMapper, OrderDetail> implements PayService {
    @Resource
    private OrderClient orderClient;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private AlipayConfig alipayConfig;


    @Override
    public RestBean<String> createPayOrder(PayCreateDTO dto) throws AlipayApiException {
        log.info("创建支付订单，订单ID: {}", dto.getOrderId());
        //判断创建这个支付的用户与订单的创建id是否一致
        Long currentId = BaseContext.getCurrentId();
        Long userId;
        OrderVO orderVO;
        try {
            orderVO = orderClient.getOrder(dto.getOrderId()).data();
            userId = orderVO.getUserId();
        } catch (FeignException e) {
            throw new BusinessException(503, "订单服务不可用");
        }
        if(Longs.compare(currentId, userId) != 0){
            return RestBean.failure(403, "错误操作，请联系管理员");
        }
        if (orderVO.getExpireTime().isBefore(LocalDateTime.now())) {
            return RestBean.failure(405, "订单已超时");
        }


        OrderDetail orderDetail = new OrderDetail();
        BeanUtils.copyProperties(dto, orderDetail);
        //存入数据库
        save(orderDetail);

        //生成支付宝支付链接
        String alipayUrl = createAlipayUrl(orderDetail.getId(), orderDetail.getRealFee());

        //将支付链接返回
        return RestBean.successWithData(alipayUrl);
    }

    /**
     * 修改订单状态
     */
    @Transactional
    public void updateOrderStatus(Long orderId, PayStatus status) throws BusinessException {
        OrderDetail order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 只有未处理的订单才更新
        if (order.getPayStatus() == PENDING.getValue()) {
            order.setPayStatus(status.getValue());
            order.setPayTime(LocalDateTime.now());
            updateById(order);
        }
        int code = orderClient.changeOrderStatus(orderId).code();
        if (code != 200) {
            throw new RuntimeException("修改订单信息失败");
        }
    }

    @Override
    public String handlerAlipayCallback(HttpServletRequest request) {
        log.info("支付宝回调参数: {}", request.getParameterMap());
        try {
            // 将回调参数转换为Map
            Map<String, String> params = convertRequestParamsToMap(request);

            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );

            if (!signVerified) {
                return "failure"; // 签名验证失败
            }

            // 解析回调参数
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 更新订单状态为成功
                updateOrderStatus(Long.parseLong(outTradeNo), PayStatus.SUCCESS);
            }
            return "success"; // 告诉支付宝已正确处理
        } catch (AlipayApiException | BusinessException e) {
            return "failure";
        }
    }


    // 将 HttpServletRequest 参数转为 Map
    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));
    }

    /**
     * 封装创建支付宝支付链接的操作
     * @param orderDetailId
     * @param totalAmount
     * @return
     * @throws AlipayApiException
     */
    private String createAlipayUrl(Long orderDetailId, Float totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        //设置参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderDetailId);
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", "商品支付");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());

        //发起请求
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            return response.getBody();
        }
        throw new BusinessException(ErrorCode.PAYMENT_FAILED);
    }
}
