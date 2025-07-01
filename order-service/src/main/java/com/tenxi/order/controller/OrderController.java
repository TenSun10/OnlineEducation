package com.tenxi.order.controller;

import com.tenxi.handler.ControllerHandler;
import com.tenxi.order.entity.dto.OrderCreateDto;
import com.tenxi.order.entity.vo.OrderVO;
import com.tenxi.order.service.OrderService;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Resource
    private OrderService orderService;
    @Resource
    private ControllerHandler controllerHandler;

    //创建订单
    @PostMapping
    public RestBean<Long> createOrder(@RequestBody OrderCreateDto dto) {
        return orderService.createOrder(dto);
    }

    //查询所有订单
    @GetMapping
    public RestBean<List<OrderVO>> getAllOrders() {
        return orderService.getAllOrders();
    }

    //查询特定订单
    @GetMapping("/{id}")
    public RestBean<OrderVO> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/status/{id}")
    public RestBean<String> changeOrderStatus(@PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                orderService.changeOrderStatus(id));
    }

    //删除订单
    @DeleteMapping("/{id}")
    public RestBean<String> deleteOrder(@PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                orderService.deleteOrderById(id));
    }

    //根据课程的id获取到所有订阅了该课程的用户的id
    @GetMapping("/subscribe/{id}")
    List<Long> subscribe(@PathVariable("id") Long id) {
        return orderService.subscribe(id);
    }
}
