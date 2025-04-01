package com.tenxi.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.pay.entity.po.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayMapper extends BaseMapper<OrderDetail> {
}
