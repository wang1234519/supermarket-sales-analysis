package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 品类销售统计
 */
@Data
public class CategorySales {

    /** 品类名称 */
    private String category;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;

    /** 销量（件） */
    private Long quantity;

    /** 销售额占比（%） */
    private BigDecimal share;
}
