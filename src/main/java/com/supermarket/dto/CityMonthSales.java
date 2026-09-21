package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市 × 月份 销售统计
 */
@Data
public class CityMonthSales {

    /** 月份，如 2026-01 */
    private String month;

    /** 城市 */
    private String city;

    /** 销售额（元） */
    private BigDecimal sales;
}
