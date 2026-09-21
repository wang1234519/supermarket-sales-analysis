package com.supermarket.service;

import com.supermarket.dto.DailySales;
import com.supermarket.dto.MonthlySales;
import com.supermarket.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 月度趋势分析服务（只读 Hive 计算结果表）
 */
@Service
@RequiredArgsConstructor
public class MonthlyService {

    private static final DateTimeFormatter MONTH_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ResultMapper resultMapper;

    /** 月度趋势（环比增长率已在 Hive 中算好） */
    public List<MonthlySales> getTrend() {
        return resultMapper.selectMonthSales();
    }

    /** 指定月份的每日销售走势（month 格式 yyyy-MM） */
    public List<DailySales> getDaily(String month) {
        try {
            YearMonth.parse(month, MONTH_PATTERN);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("月份格式不正确，应为 yyyy-MM，例如 2026-03");
        }
        return resultMapper.selectDailySales(month);
    }
}
