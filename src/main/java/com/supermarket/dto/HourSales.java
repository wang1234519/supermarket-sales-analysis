package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 时段（小时）销售统计
 */
@Data
public class HourSales {

    /** 小时（0-23） */
    private Integer hour;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;

    /** 销量（件） */
    private Long quantity;
}
