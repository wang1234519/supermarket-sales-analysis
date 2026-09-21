package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度销售统计
 */
@Data
public class MonthlySales {

    /** 月份，如 2026-01 */
    private String month;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;

    /** 销量（件） */
    private Long quantity;

    /** 客单价（元） */
    private BigDecimal avgOrderAmount;

    /** 环比增长率（%），首月为 null */
    private BigDecimal growthRate;
}
