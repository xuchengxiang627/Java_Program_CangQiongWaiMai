package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.temPojo.NewUser;
import com.sky.temPojo.OrderReport;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;


    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Orders::getOrderTime, begin.atStartOfDay(), end.atTime(23, 59, 59))
                .select(Orders::getOrderTime, Orders::getAmount)
                .eq(Orders::getStatus, Orders.COMPLETED)
                .orderByAsc(Orders::getOrderTime);
        List<Orders> ordersList = orderMapper.selectList(queryWrapper);

        List<LocalDate> date = new ArrayList<>();
        List<BigDecimal> amount = new ArrayList<>();

        int i = 0, size = ordersList.size();
        while (!begin.isAfter(end)) {
            date.add(begin);
            BigDecimal totalAmount = new BigDecimal(0);
            while (i < size && ordersList.get(i).getOrderTime().toLocalDate().equals(begin)) {
                totalAmount = totalAmount.add(ordersList.get(i).getAmount());
                i++;
            }
            amount.add(totalAmount);
            begin = begin.plusDays(1);
        }

        TurnoverReportVO vo = TurnoverReportVO.builder()
                .turnoverList(StringUtils.join(amount, ','))
                .dateList(StringUtils.join(date, ','))
                .build();
        log.info("vo:{}", vo);
        return vo;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(User::getCreateTime, begin.atStartOfDay());
        long totalUser = userMapper.selectCount(queryWrapper);


        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("begin", begin.atStartOfDay());
        map.put("end", end.atTime(23, 59, 59));
        List<NewUser> newUserList = userMapper.selectNewUser(map);

        log.info("newUserList:{}", newUserList);

        List<LocalDate> dateList = new ArrayList<>();
        List<Long> newUserList2 = new ArrayList<>();
        List<Long> totalUserList = new ArrayList<>();

        int i = 0, size = newUserList.size();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            if (i < size && newUserList.get(i).getCreateTime().equals(begin)) {
                newUserList2.add(newUserList.get(i).getNum());
                totalUser += newUserList.get(i).getNum();
                totalUserList.add(totalUser);
                i++;
            } else {
                newUserList2.add(0L);
                totalUserList.add(totalUser);
            }
            begin = begin.plusDays(1);
        }

        UserReportVO vo = UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .newUserList(StringUtils.join(newUserList2, ','))
                .totalUserList(StringUtils.join(totalUserList, ','))
                .build();

        log.info("UserReportVO:{}", vo);

        return vo;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {


        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        List<OrderReport> orderReportList = orderMapper.selectOrderReport(dateList);
        log.info("dateList:{}", dateList);
        log.info("orderReportList:{}", orderReportList);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;

        for (int i = 0, j = 0; i < dateList.size(); i++) {
            while (i < dateList.size() && (j >= orderReportList.size() || dateList.get(i).isBefore(orderReportList.get(j).getDate()))) {
                orderCountList.add(0);
                validOrderCountList.add(0);
                i++;
            }
            if (j < orderReportList.size()) {
                orderCountList.add(orderReportList.get(j).getTotalOrderCount());
                validOrderCountList.add(orderReportList.get(j).getValidOrderCount());
                totalOrderCount += orderReportList.get(j).getTotalOrderCount();
                validOrderCount += orderReportList.get(j).getValidOrderCount();
                j++;
            }
        }

        OrderReportVO vo = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .orderCountList(StringUtils.join(orderCountList, ','))
                .validOrderCountList(StringUtils.join(validOrderCountList, ','))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(totalOrderCount == 0 ? 0.0 : validOrderCount / (double) totalOrderCount)
                .build();

        log.info("OrderReportVO:{}", vo);
        return vo;
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.getSalesTop10(begin, end.plusDays(1));
        List<String> nameList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).toList();
        List<Integer> numberList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).toList();
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ','))
                .numberList(StringUtils.join(numberList, ','))
                .build();
    }
}
