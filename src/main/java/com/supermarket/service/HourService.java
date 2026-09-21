package com.supermarket.service;

import com.supermarket.dto.HeatCell;
import com.supermarket.dto.HourSales;
import com.supermarket.dto.HourTypeSales;
import com.supermarket.dto.WeekdaySales;
import com.supermarket.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 时段分析服务（只读 Hive 计算结果表）
 */
@Service
@RequiredArgsConstructor
public class HourService {

    /** 星期名称（下标 1=周一 ... 7=周日） */
    private static final String[] DAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final ResultMapper resultMapper;

    /** 各时段（小时）销售分布 */
    public List<HourSales> getDistribution() {
        return resultMapper.selectHourSales();
    }

    /** 各星期销售统计，缺数据的星期补 0，保证图表 7 天完整 */
    public List<WeekdaySales> getWeekday() {
        Map<Integer, WeekdaySales> map = resultMapper.selectWeekdaySales().stream()
                .collect(Collectors.toMap(WeekdaySales::getDayOfWeek, Function.identity()));
        return java.util.stream.IntStream.rangeClosed(1, 7).mapToObj(i -> {
            WeekdaySales w = map.get(i);
            if (w == null) {
                w = new WeekdaySales();
                w.setDayOfWeek(i);
                w.setSales(BigDecimal.ZERO);
                w.setOrders(0L);
            }
            w.setDayName(DAY_NAMES[i]);
            return w;
        }).collect(Collectors.toList());
    }

    /** 工作日 / 周末 × 小时 对比 */
    public List<HourTypeSales> getWeekdayCompare() {
        return resultMapper.selectHourWeekdaySales();
    }

    /** 星期 × 小时 热力图 */
    public List<HeatCell> getHeatmap() {
        return resultMapper.selectHeatmapSales();
    }
}
