package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ") // 每分钟执行一次
    public void processTimeoutOrder() {
        log.info("定时处理处于待支付状态的订单");
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .le(Orders::getOrderTime, LocalDateTime.now().plusMinutes(-15));

        List<Orders> ordersList = orderMapper.selectList(queryWrapper);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.updateById(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ? ") // 每天凌晨1点执行一次
    public void processDeliveryOrder() {
        log.info("定时处理处于待派送状态的订单");
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .le(Orders::getOrderTime, LocalDateTime.now().plusHours(-1));

        List<Orders> ordersList = orderMapper.selectList(queryWrapper);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.updateById(orders);
            }
        }
    }

}
