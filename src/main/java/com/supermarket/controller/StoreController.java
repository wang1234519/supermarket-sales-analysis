package com.supermarket.controller;

import com.supermarket.dto.CityMonthSales;
import com.supermarket.dto.CitySales;
import com.supermarket.dto.StoreDetail;
import com.supermarket.dto.StoreInfo;
import com.supermarket.dto.StoreSales;
import com.supermarket.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店 / 城市分析接口（读取 Hive 计算结果表）
 */
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** 全部门店业绩排行 */
    @GetMapping("/ranking")
    public List<StoreSales> ranking() {
        return storeService.getRanking();
    }

    /** 城市消费力排行 */
    @GetMapping("/cities")
    public List<CitySales> cities() {
        return storeService.getCities();
    }

    /** 城市 × 月份 销售趋势 */
    @GetMapping("/city-trend")
    public List<CityMonthSales> cityTrend() {
        return storeService.getCityTrend();
    }

    /** 门店列表（详情页下拉框） */
    @GetMapping("/stores")
    public List<StoreInfo> stores() {
        return storeService.getStores();
    }

    /** 门店详情 */
    @GetMapping("/{storeId}/detail")
    public StoreDetail detail(@PathVariable Integer storeId) {
        return storeService.getDetail(storeId);
    }
}
