package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Tag(name = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PutMapping("/cancel")
    @Operation(summary = "取消订单")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderService.getById(ordersCancelDTO.getId());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());

        orderService.saveOrUpdate(orders);

        // todo: 订单取消，若已付款，则退款
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO result = new OrderStatisticsVO();

        LambdaQueryWrapper<Orders> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Orders::getStatus, Orders.TO_BE_CONFIRMED);
        int toBeConfirmed = (int) orderService.count(queryWrapper1);

        LambdaQueryWrapper<Orders> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Orders::getStatus, Orders.CONFIRMED);
        int confirmed = (int) orderService.count(queryWrapper2);

        LambdaQueryWrapper<Orders> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS);
        int deliveryInProgress = (int) orderService.count(queryWrapper3);

        result.setToBeConfirmed(toBeConfirmed);
        result.setConfirmed(confirmed);
        result.setDeliveryInProgress(deliveryInProgress);
        return Result.success(result);
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "订单完成")
    public Result<String> complete(@PathVariable("id") Long id) {
        Orders orders = orderService.getById(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderService.saveOrUpdate(orders);
        return Result.success();
    }

    @PutMapping("/rejection")
    @Operation(summary = "拒单")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderService.getById(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderService.saveOrUpdate(orders);

        // todo: 订单取消，若已付款，则退款
        return Result.success();
    }

    @PutMapping("/confirm")
    @Operation(summary = "接单")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderService.getById(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        orderService.saveOrUpdate(orders);
        return Result.success();
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        OrderVO orders = orderService.orderDetail(id);
        orders.setOrderDishes("不知道是什么意思的订单菜品");
        return Result.success(orders);
    }

    @PutMapping("/delivery/{id}")
    @Operation(summary = "订单配送")
    public Result<String> delivery(@PathVariable("id") Long id) {
        Orders orders = orderService.getById(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setDeliveryTime(LocalDateTime.now());
        orderService.saveOrUpdate(orders);
        return Result.success();
    }

    @GetMapping("/conditionSearch")
    @Operation(summary = "条件搜索订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        IPage<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ordersPageQueryDTO.getPhone() != null && !ordersPageQueryDTO.getPhone().isEmpty(), Orders::getPhone, ordersPageQueryDTO.getPhone())
                .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                .eq(ordersPageQueryDTO.getNumber() != null && !ordersPageQueryDTO.getNumber().isEmpty(), Orders::getNumber, ordersPageQueryDTO.getNumber())
                .ge(ordersPageQueryDTO.getBeginTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getBeginTime())
                .le(ordersPageQueryDTO.getEndTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getEndTime())
                .orderByDesc(Orders::getOrderTime);
        IPage<Orders> pageOrder = orderService.page(page, queryWrapper);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageOrder.getTotal());
        pageResult.setRecords(pageOrder.getRecords());
        return Result.success(pageResult);
    }
}
