package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工作日 / 周末 × 小时 销售统计
 */
@Data
public class HourTypeSales {

    /** 小时（0-23） */
    private Integer hour;

    /** weekday=工作日，weekend=周末 */
    private String type;

    /** 销售额（元） */
    private BigDecimal sales;

    /** 订单数 */
    private Long orders;
}
