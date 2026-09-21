package com.supermarket.service;

import com.supermarket.dto.CategorySales;
import com.supermarket.dto.HourSales;
import com.supermarket.dto.KpiSummary;
import com.supermarket.dto.MonthlySales;
import com.supermarket.dto.StoreSales;
import com.supermarket.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 总览大屏数据服务（只读 Hive 计算结果表）
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ResultMapper resultMapper;

    /** 核心指标卡；结果表为空时返回全 0，前端据此提示"待导出数据" */
    public KpiSummary getKpi() {
        KpiSummary kpi = resultMapper.selectKpi();
        if (kpi == null) {
            kpi = new KpiSummary();
            kpi.setTotalSales(BigDecimal.ZERO);
            kpi.setTotalOrders(0L);
            kpi.setTotalQuantity(0L);
            kpi.setAvgOrderAmount(BigDecimal.ZERO);
        }
        kpi.setStoreCount((int) resultMapper.countStores());
        kpi.setProductCount((int) resultMapper.countProducts());
        return kpi;
    }

    /** 月度销售趋势 */
    public List<MonthlySales> getMonthly() {
        return resultMapper.selectMonthSales();
    }

    /** 品类销售占比 */
    public List<CategorySales> getCategoryShare() {
        return resultMapper.selectCategorySales();
    }

    /** 门店业绩 TOP N */
    public List<StoreSales> getStoreTop(int limit) {
        List<StoreSales> all = resultMapper.selectStoreSales();
        return all.subList(0, Math.min(Math.max(1, limit), all.size()));
    }

    /** 各时段销售分布 */
    public List<HourSales> getHourly() {
        return resultMapper.selectHourSales();
    }
}
