package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.temPojo.OrderReport;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    IPage<OrderVO> historyOrders(IPage<OrderVO> page, Long currentId, Integer status);

    List<OrderReport> selectOrderReport(List<LocalDate> dateList);

    List<GoodsSalesDTO> getSalesTop10(LocalDate begin, LocalDate end);
}
