package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    IPage<OrderVO> historyOrders(IPage<OrderVO> page, Long currentId, Integer status);
}
