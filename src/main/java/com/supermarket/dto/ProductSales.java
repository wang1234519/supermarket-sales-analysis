package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品销售统计
 */
@Data
public class ProductSales {

    /** 商品编号 */
    private Integer productId;

    /** 商品名称 */
    private String productName;

    /** 品类 */
    private String category;

    /** 单价（元） */
    private BigDecimal price;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 销量（件） */
    private Long quantity;

    /** 成交笔数 */
    private Long orders;
}
