package com.supermarket.controller;

import com.supermarket.dto.HeatCell;
import com.supermarket.dto.HourSales;
import com.supermarket.dto.HourTypeSales;
import com.supermarket.dto.WeekdaySales;
import com.supermarket.service.HourService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 时段分析接口
 */
@RestController
@RequestMapping("/api/hour")
@RequiredArgsConstructor
public class HourController {

    private final HourService hourService;

    /** 各时段（小时）销售分布 */
    @GetMapping("/distribution")
    public List<HourSales> distribution() {
        return hourService.getDistribution();
    }

    /** 各星期销售统计（周一到周日完整 7 天） */
    @GetMapping("/weekday")
    public List<WeekdaySales> weekday() {
        return hourService.getWeekday();
    }

    /** 工作日 / 周末 × 小时 对比 */
    @GetMapping("/weekday-compare")
    public List<HourTypeSales> weekdayCompare() {
        return hourService.getWeekdayCompare();
    }

    /** 星期 × 小时 热力图 */
    @GetMapping("/heatmap")
    public List<HeatCell> heatmap() {
        return hourService.getHeatmap();
    }
}
