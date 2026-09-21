package com.supermarket.controller;

import com.supermarket.dto.CategorySales;
import com.supermarket.dto.HourSales;
import com.supermarket.dto.KpiSummary;
import com.supermarket.dto.MonthlySales;
import com.supermarket.dto.StoreSales;
import com.supermarket.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 总览大屏接口（读取 Hive 计算结果表）
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 核心指标卡 */
    @GetMapping("/summary")
    public KpiSummary summary() {
        return dashboardService.getKpi();
    }

    /** 月度销售趋势 */
    @GetMapping("/monthly")
    public List<MonthlySales> monthly() {
        return dashboardService.getMonthly();
    }

    /** 品类销售占比 */
    @GetMapping("/category-share")
    public List<CategorySales> categoryShare() {
        return dashboardService.getCategoryShare();
    }

    /** 门店业绩 TOP N */
    @GetMapping("/store-top")
    public List<StoreSales> storeTop(@RequestParam(defaultValue = "10") int limit) {
        return dashboardService.getStoreTop(limit);
    }

    /** 各时段（小时）销售分布 */
    @GetMapping("/hourly")
    public List<HourSales> hourly() {
        return dashboardService.getHourly();
    }
}
