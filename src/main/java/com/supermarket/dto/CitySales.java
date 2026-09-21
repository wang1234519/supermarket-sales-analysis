package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市销售统计
 */
@Data
public class CitySales {

    /** 城市 */
    private String city;

    /** 门店数 */
    private Integer storeCount;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;

    /** 销售额占比（%） */
    private BigDecimal share;
}
