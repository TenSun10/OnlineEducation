package com.tenxi.pay.listener;

import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.PayProcessException;
import com.tenxi.pay.client.OrderClient;
import com.tenxi.pay.entity.po.CourseSubscribe;
import com.tenxi.pay.entity.po.OrderDetail;
import com.tenxi.pay.entity.vo.OrderVO;
import com.tenxi.pay.enums.PayStatus;
import com.tenxi.pay.mapper.CourseSubscribeMapper;
import com.tenxi.pay.mapper.PayMapper;
import com.tenxi.pay.service.PayService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(
                        name = "online.education.pay",
                        durable = "true" // 队列持久化
                ),
                exchange = @Exchange(
                        name = "online.direct",
                        type = ExchangeTypes.DIRECT,
                        durable = "true" // 交换机持久化
                ),
                key = {"online-education-pay"}
        )
)
public class PayListener {

    @Resource
    private PayMapper payMapper;

    @Resource
    private CourseSubscribeMapper courseSubscribeMapper;

    @Resource
    private OrderClient orderClient;

    @RabbitHandler
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(Map<String, Object> message, Message amqpMessage) {
        try {
            Long orderId = (Long) message.get("orderId");
            Float totalAmount = Float.parseFloat(message.get("totalAmount").toString());

            log.info("处理支付成功消息: orderId={}", orderId);

            // 1. 创建支付详情记录
            createOrderDetail(orderId, totalAmount);

            // 2. 创建课程订阅记录
            createCourseSubscribe(orderId);


        } catch (Exception e) {
            log.error("处理支付消息失败", e);
            // 可根据业务需求重试或进入死信队列
        }
    }

    private void createOrderDetail(Long orderId, Float totalAmount) {
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setPayType(1); // 支付宝支付
        detail.setRealFee(totalAmount);
        detail.setPayTime(LocalDateTime.now());
        detail.setPayStatus(PayStatus.SUCCESS.getValue());
        if(payMapper.insert(detail) <= 0) {
            throw new PayProcessException(ErrorCode.PAYMENT_DETAIL_SET_FAILED);
        }
    }

    private void createCourseSubscribe(Long orderId) {
        // 通过Feign获取订单详情
        OrderVO order = orderClient.getOrder(orderId).data();

        CourseSubscribe subscribe = new CourseSubscribe();
        subscribe.setCourseId(order.getCourseId());
        subscribe.setAccountId(order.getUserId());
        subscribe.setOrderId(orderId);
        subscribe.setSubscribeTime(LocalDateTime.now());

        if(courseSubscribeMapper.insert(subscribe) <= 0) {
            throw new PayProcessException(ErrorCode.PAYMENT_SUBSCRIBE_SET_FAILED);
        }
        log.info("课程订阅已创建: courseId={}, userId={}",
                order.getCourseId(), order.getUserId());
    }
}