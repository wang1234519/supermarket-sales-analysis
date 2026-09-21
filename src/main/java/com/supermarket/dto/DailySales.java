package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单月内每日销售统计
 */
@Data
public class DailySales {

    /** 日期（MM-dd） */
    private String day;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;
}
