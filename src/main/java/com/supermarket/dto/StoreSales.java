package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店销售统计
 */
@Data
public class StoreSales {

    /** 门店编号 */
    private Integer storeId;

    /** 门店名称 */
    private String storeName;

    /** 所在城市 */
    private String city;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;

    /** 销量（件） */
    private Long quantity;

    /** 客单价（元） */
    private BigDecimal avgOrderAmount;
}
