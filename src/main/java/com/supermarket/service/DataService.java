package com.supermarket.service;

import com.supermarket.dto.PageResult;
import com.supermarket.mapper.DataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果表数据浏览服务
 *
 * 只读展示 16 张结果表的明细数据；表名白名单校验，防止 SQL 注入。
 */
@Service
@RequiredArgsConstructor
public class DataService {

    /** 表名白名单（顺序即页面展示顺序） */
    private static final Map<String, String> TABLES = new LinkedHashMap<>();

    static {
        TABLES.put("result_kpi", "总览 KPI");
        TABLES.put("result_category_sales", "品类销售");
        TABLES.put("result_hour_sales", "时段销售");
        TABLES.put("result_month_sales", "月度销售");
        TABLES.put("result_store_sales", "门店业绩");
        TABLES.put("result_city_sales", "城市消费力");
        TABLES.put("result_product_sales", "商品销售");
        TABLES.put("result_weekday_sales", "星期销售");
        TABLES.put("result_hour_weekday_sales", "时段×工作日/周末");
        TABLES.put("result_heatmap_sales", "星期×时段热力图");
        TABLES.put("result_daily_sales", "每日销售");
        TABLES.put("result_category_month_sales", "品类×月份趋势");
        TABLES.put("result_city_month_sales", "城市×月份趋势");
        TABLES.put("result_store_month_sales", "门店×月份");
        TABLES.put("result_store_category_sales", "门店×品类");
        TABLES.put("result_store_product_sales", "门店×商品");
    }

    private final DataMapper dataMapper;

    /** 全部结果表清单 */
    public List<Map<String, String>> getTables() {
        List<Map<String, String>> list = new ArrayList<>();
        TABLES.forEach((name, label) -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("label", label);
            list.add(item);
        });
        return list;
    }

    /** 指定结果表的分页数据 */
    public PageResult<Map<String, Object>> getData(String table, int page, int size) {
        if (!TABLES.containsKey(table)) {
            throw new IllegalArgumentException("非法的表名：" + table);
        }
        if (page < 1) {
            page = 1;
        }
        size = Math.max(1, Math.min(size, 100));
        List<Map<String, Object>> list = dataMapper.selectRows(table, (page - 1) * size, size);
        long total = dataMapper.countRows(table);
        return new PageResult<>(list, total, page, size);
    }
}
