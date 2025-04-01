package com.tenxi.order.controller;

import com.tenxi.handler.ControllerHandler;
import com.tenxi.order.entity.dto.OrderCreateDto;
import com.tenxi.order.entity.vo.OrderVO;
import com.tenxi.order.service.OrderService;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
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

    //删除订单
    @DeleteMapping("/{id}")
    public RestBean<String> deleteOrder(@PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                orderService.deleteOrderById(id));
    }
}
