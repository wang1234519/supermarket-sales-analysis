package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 品类 × 月份 销售统计（用于多系列趋势图）
 */
@Data
public class CategoryMonthSales {

    /** 月份，如 2026-01 */
    private String month;

    /** 品类名称 */
    private String category;

    /** 销售额（元） */
    private BigDecimal sales;
}
