package com.tenxi.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.order.client.CourseClient;
import com.tenxi.order.entity.dto.OrderCreateDto;
import com.tenxi.order.entity.po.Order;
import com.tenxi.order.entity.vo.CourseSimpleVO;
import com.tenxi.order.entity.vo.OrderVO;
import com.tenxi.order.mapper.OrderMapper;
import com.tenxi.order.service.OrderService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Resource
    private CourseClient courseClient;

    /**
     * 用户创建订单操作
     * @param dto
     * @return
     */
    @Override
    public RestBean<Long> createOrder(OrderCreateDto dto) {
        //获取课程相关信息
        CourseSimpleVO courseVO = courseClient.getCourse(dto.getCourseId());

        //获取下单用户的信息
        Long userId = BaseContext.getCurrentId();

        //先只保存Order,设置一个订单支付时间期限,未到期限之前支付再保存OrderDetail
        Order order = new Order();

        order.setUserId(userId);
        order.setCourseId(dto.getCourseId());
        order.setPusherId(courseVO.getPusherId());
        order.setCreateTime(LocalDateTime.now());
        order.setTotalFee(courseVO.getDiscountPrice());
        //期限为15分钟
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));
        save(order);

        //创建成功将订单id返回
        return RestBean.successWithData(order.getId());
    }

    /**
     * 查询用户所有订单
     * 包括支付和未支付
     * @return
     */
    @Override
    public RestBean<List<OrderVO>> getAllOrders() {
        //获取下单用户的信息
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        List<Order> orders = list(queryWrapper);

        List<OrderVO> list = orders.stream().map(this::transOrderToVO).toList();


        return RestBean.successWithData(list);
    }

    @Override
    public RestBean<OrderVO> getOrderById(Long id) {
        //获取下单用户的信息
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.eq(Order::getId, id);

        Order item = getOne(queryWrapper);

        if(item == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        OrderVO orderVO = transOrderToVO(item);

        return RestBean.successWithData(orderVO);
    }

    /**
     * 用户取消订单
     * @param id
     * @return
     */
    @Override
    public RestBean<String> deleteOrderById(Long id) {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getId, id).eq(Order::getUserId, currentId);
        boolean remove = remove(queryWrapper);
        if(remove) return RestBean.successWithMsg("订单删除成功");
        throw new BusinessException(ErrorCode.ORDER_DEL_FAILED);
    }


    /**
     * 根据课程id查询所有订阅该课程的用户
     * @param id
     * @return
     */
    @Override
    public List<Long> subscribe(Long id) {
        List<Long> list = new ArrayList<>();
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getCourseId, id);
        List<Order> orders = list(queryWrapper);
        if (orders == null || orders.size() == 0) {
            return List.of();
        }
        for(Order order : orders) {
            list.add(order.getUserId());
        }
        return list;
    }

    @Override
    public RestBean<String> changeOrderStatus(Long id) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getId, id);
        Order one = getOne(queryWrapper);
        if(one == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        one.setStatus(1);
        if(updateById(one)) return RestBean.successWithMsg("修改订单状态成功");

        throw new BusinessException(ErrorCode.ORDER_STATUS_UPDATE_FAILED);
    }

    /**
     * 封装转换为VO的方法
     * @param item
     * @return
     */
    public OrderVO transOrderToVO(Order item) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(item, orderVO);

        //获取到课程相关信息
        CourseSimpleVO course = courseClient.getCourse(item.getCourseId());
        orderVO.setCourseDes(course.getIntroduction());
        orderVO.setCourseName(course.getTitle());
        orderVO.setNowFee(course.getDiscountPrice());
        return orderVO;
    }

    /**
     * 定时任务清楚过期订单
     */
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void cleanExpiredOrders() {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, 0) // 未支付的订单
                .lt(Order::getExpireTime, LocalDateTime.now()); // 过期的

        List<Order> expiredOrders = list(queryWrapper);
        if (!expiredOrders.isEmpty()) {
            remove(queryWrapper);
            log.info("清理了"+ expiredOrders.size() + "条过期订单");
        }
    }
}
