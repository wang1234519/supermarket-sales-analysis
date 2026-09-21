package com.supermarket.controller;

import com.supermarket.dto.DailySales;
import com.supermarket.dto.MonthlySales;
import com.supermarket.service.MonthlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 月度趋势分析接口
 */
@RestController
@RequestMapping("/api/monthly")
@RequiredArgsConstructor
public class MonthlyController {

    private final MonthlyService monthlyService;

    /** 月度趋势（含环比增长率） */
    @GetMapping("/trend")
    public List<MonthlySales> trend() {
        return monthlyService.getTrend();
    }

    /** 指定月份的每日销售走势（month 格式 yyyy-MM） */
    @GetMapping("/daily")
    public List<DailySales> daily(@RequestParam String month) {
        return monthlyService.getDaily(month);
    }
}
