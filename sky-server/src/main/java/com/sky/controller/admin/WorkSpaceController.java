package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/workspace")
@Tag(name = "工作台接口")
public class WorkSpaceController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;


    @GetMapping("/businessData")
    @Operation(summary = "查询今日运营数据")
    public Result<BusinessDataVO> businessData() {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Orders::getOrderTime, LocalDate.now().atStartOfDay())
                .le(Orders::getOrderTime, LocalDate.now().atTime(23, 59, 59));
        List<Orders> orders = orderMapper.selectList(queryWrapper);

        double turnover = 0.0;
        int validOrderCount = 0;
        for (Orders order : orders) {
            if (order.getStatus() == Orders.COMPLETED) {
                turnover += order.getAmount().doubleValue();
                validOrderCount++;
            }
        }

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.ge(User::getCreateTime, LocalDate.now().atStartOfDay())
                .le(User::getCreateTime, LocalDate.now().atTime(23, 59, 59));
        int newUsers = userMapper.selectCount(userQueryWrapper).intValue();

        BusinessDataVO businessDataVO = BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orders.isEmpty() ? 0.0 : validOrderCount / (double) orders.size())
                .unitPrice(validOrderCount == 0 ? 0.0 : turnover / validOrderCount)
                .newUsers(newUsers)
                .build();
        return Result.success(businessDataVO);
    }

    @GetMapping("/overviewSetmeals")
    @Operation(summary = "查询套餐总览")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO.builder()
                .sold(setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, StatusConstant.ENABLE)).intValue())
                .discontinued(setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, StatusConstant.DISABLE)).intValue())
                .build();
        return Result.success(setmealOverViewVO);
    }

    @GetMapping("/overviewDishes")
    @Operation(summary = "查询菜品总览")
    public Result<DishOverViewVO> overviewDishes() {
        DishOverViewVO dishOverViewVO = DishOverViewVO.builder()
                .sold(dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, StatusConstant.ENABLE)).intValue())
                .discontinued(dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, StatusConstant.DISABLE)).intValue())
                .build();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewOrders")
    @Operation(summary = "查询订单管理数据")
    public Result<OrderOverViewVO> overviewOrders() {
        OrderOverViewVO orderOverViewVO = orderMapper.selectOrderOverView();
        return Result.success(orderOverViewVO);
    }
}
