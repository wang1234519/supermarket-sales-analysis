package com.supermarket.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 总览页核心指标（来自 Hive 计算结果表 result_kpi）
 */
@Data
public class KpiSummary {

    /** 总销售额（元） */
    private BigDecimal totalSales;

    /** 订单总数（去重订单号） */
    private Long totalOrders;

    /** 商品总销量（件） */
    private Long totalQuantity;

    /** 客单价（元）= 总销售额 / 订单数 */
    private BigDecimal avgOrderAmount;

    /** 门店数（来自结果表行数） */
    private Integer storeCount;

    /** 商品种类数（来自结果表行数） */
    private Integer productCount;
}
