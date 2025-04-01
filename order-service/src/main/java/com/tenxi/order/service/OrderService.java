package com.tenxi.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.order.entity.dto.OrderCreateDto;
import com.tenxi.order.entity.po.Order;
import com.tenxi.order.entity.vo.OrderVO;
import com.tenxi.utils.RestBean;

import java.util.List;

public interface OrderService extends IService<Order> {
    RestBean<Long> createOrder(OrderCreateDto dto);

    RestBean<List<OrderVO>> getAllOrders();

    RestBean<OrderVO> getOrderById(Long id);

    String deleteOrderById(Long id);
}
