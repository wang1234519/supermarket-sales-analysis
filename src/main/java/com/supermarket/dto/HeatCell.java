package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 星期 × 小时 热力图单元格
 */
@Data
public class HeatCell {

    /** 1=周一 ... 7=周日（与 WeekdaySales 一致） */
    private Integer dayOfWeek;

    /** 小时（0-23） */
    private Integer hour;

    /** 销售额（元） */
    private BigDecimal sales;
}
