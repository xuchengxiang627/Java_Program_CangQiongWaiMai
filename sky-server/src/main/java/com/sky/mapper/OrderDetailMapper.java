package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜品
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    void insertBatch(List<OrderDetail> orderDetailList);

}
