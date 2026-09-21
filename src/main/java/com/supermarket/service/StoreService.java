package com.supermarket.service;

import com.supermarket.dto.CityMonthSales;
import com.supermarket.dto.CitySales;
import com.supermarket.dto.StoreDetail;
import com.supermarket.dto.StoreInfo;
import com.supermarket.dto.StoreSales;
import com.supermarket.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门店 / 城市分析服务（只读 Hive 计算结果表）
 */
@Service
@RequiredArgsConstructor
public class StoreService {

    private final ResultMapper resultMapper;

    /** 全部门店业绩排行 */
    public List<StoreSales> getRanking() {
        return resultMapper.selectStoreSales();
    }

    /** 城市消费力排行 */
    public List<CitySales> getCities() {
        return resultMapper.selectCitySales();
    }

    /** 城市 × 月份 销售趋势 */
    public List<CityMonthSales> getCityTrend() {
        return resultMapper.selectCityMonthSales();
    }

    /** 门店列表（详情页下拉框） */
    public List<StoreInfo> getStores() {
        return resultMapper.selectStores();
    }

    /** 门店详情：概况 + 月度走势 + 品类构成 + 热销商品 */
    public StoreDetail getDetail(Integer storeId) {
        StoreSales overview = resultMapper.selectStoreSales().stream()
                .filter(s -> s.getStoreId().equals(storeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "结果表中不存在该门店：storeId=" + storeId + "，请确认 Hive 分析已执行且 Sqoop 已导出"));

        StoreDetail detail = new StoreDetail();
        detail.setOverview(overview);
        detail.setMonthly(resultMapper.selectStoreMonthSales(storeId));
        detail.setCategories(resultMapper.selectStoreCategorySales(storeId));
        detail.setTopProducts(resultMapper.selectStoreProductSales(storeId, 10));
        return detail;
    }
}
