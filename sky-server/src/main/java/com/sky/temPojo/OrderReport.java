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
public class OrderReport implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;
    private int validOrderCount;
    private int totalOrderCount;
}
