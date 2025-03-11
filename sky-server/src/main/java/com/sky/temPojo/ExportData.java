package com.sky.temPojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportData implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate date;

    private Double turnover;//营业额

    private Integer validOrderCount;//有效订单数

    private Double orderCompletionRate;//订单完成率

    private Double unitPrice;//平均客单价

    private Integer newUsers;//新增用户数
}
