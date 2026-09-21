package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 星期销售统计
 */
@Data
public class WeekdaySales {

    /** 1=周一 ... 7=周日 */
    private Integer dayOfWeek;

    /** 星期名称（周一~周日） */
    private String dayName;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;
}
