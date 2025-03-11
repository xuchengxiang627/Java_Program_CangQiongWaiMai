package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.temPojo.ExportData;
import com.sky.temPojo.NewUser;
import com.sky.temPojo.OrderReport;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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

    @Override
    public void export(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        List<ExportData> exportDataList = orderMapper.selectExportData(begin, end);
        log.info("exportDataList:{}", exportDataList);

        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("begin", begin.atStartOfDay());
        map.put("end", end.atTime(23, 59, 59));
        List<NewUser> newUserList = userMapper.selectNewUser(map);
        log.info("newUserList:{}", newUserList);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook workbook = null;
        ServletOutputStream outputStream = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);


            double totalTurnover = exportDataList.stream().mapToDouble(ExportData::getTurnover).sum();
            int totalValidOrderCount = exportDataList.stream().mapToInt(ExportData::getValidOrderCount).sum();
            int totalOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().between(Orders::getOrderTime, begin.atStartOfDay(), end.atTime(23, 59, 59))).intValue();
            double totalOrderCompletionRate = totalOrderCount == 0 ? 0 : totalValidOrderCount / (double) totalOrderCount;
            double totalUnitPrice = totalValidOrderCount == 0 ? 0 : totalTurnover / totalValidOrderCount;
            int totalNewUsers = (int) newUserList.stream().mapToLong(NewUser::getNum).sum();

            sheet.getRow(3).getCell(2).setCellValue(totalTurnover);
            sheet.getRow(3).getCell(4).setCellValue(totalOrderCompletionRate);
            sheet.getRow(3).getCell(6).setCellValue(totalNewUsers);
            sheet.getRow(4).getCell(2).setCellValue(totalValidOrderCount);
            sheet.getRow(4).getCell(4).setCellValue(totalUnitPrice);

            LocalDate date = begin;
            int i = 0, j = 0;
            for (int row = 7; row < 37; row++) {
                XSSFRow r = sheet.getRow(row);
                r.getCell(1).setCellValue(String.valueOf(date));
                if (i < exportDataList.size() && date.equals(exportDataList.get(i).getDate())) {
                    r.getCell(2).setCellValue(exportDataList.get(i).getTurnover());
                    r.getCell(3).setCellValue(exportDataList.get(i).getValidOrderCount());
                    r.getCell(4).setCellValue(exportDataList.get(i).getOrderCompletionRate());
                    r.getCell(5).setCellValue(exportDataList.get(i).getUnitPrice());
                    i++;
                } else {
                    r.getCell(2).setCellValue(0);
                    r.getCell(3).setCellValue(0);
                    r.getCell(4).setCellValue(0);
                    r.getCell(5).setCellValue(0);
                }

                if (j < newUserList.size() && date.equals(newUserList.get(j).getCreateTime())) {
                    r.getCell(6).setCellValue(newUserList.get(j).getNum());
                    j++;
                } else {
                    r.getCell(6).setCellValue(0);
                }
                date = date.plusDays(1);
            }
            outputStream = response.getOutputStream();
            workbook.write(outputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
