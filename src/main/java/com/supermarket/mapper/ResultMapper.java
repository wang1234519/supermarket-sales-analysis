package com.supermarket.mapper;

import com.supermarket.dto.CategoryMonthSales;
import com.supermarket.dto.CategorySales;
import com.supermarket.dto.CityMonthSales;
import com.supermarket.dto.CitySales;
import com.supermarket.dto.DailySales;
import com.supermarket.dto.HeatCell;
import com.supermarket.dto.HourSales;
import com.supermarket.dto.HourTypeSales;
import com.supermarket.dto.KpiSummary;
import com.supermarket.dto.MonthlySales;
import com.supermarket.dto.ProductSales;
import com.supermarket.dto.StoreInfo;
import com.supermarket.dto.StoreSales;
import com.supermarket.dto.WeekdaySales;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 结果表读取 Mapper
 *
 * 只做展示所需的读取查询（WHERE / ORDER BY / LIMIT），
 * 所有统计计算已在虚拟机 Hive 中完成，结果经 Sqoop 导出到 MySQL。
 */
public interface ResultMapper {

    // ==================== 总览 ====================

    /** KPI（result_kpi，1 行；表为空时返回 null） */
    KpiSummary selectKpi();

    /** 门店数（result_store_sales 行数） */
    long countStores();

    /** 商品数（result_product_sales 行数） */
    long countProducts();

    /** 月度销售趋势（result_month_sales） */
    List<MonthlySales> selectMonthSales();

    // ==================== 品类 ====================

    /** 品类销售（result_category_sales） */
    List<CategorySales> selectCategorySales();

    /** 品类×月份趋势（result_category_month_sales） */
    List<CategoryMonthSales> selectCategoryMonthSales();

    /** 指定品类的商品销售排行（result_product_sales） */
    List<ProductSales> selectCategoryProducts(@Param("category") String category,
                                              @Param("limit") int limit);

    // ==================== 时段 ====================

    /** 时段销售（result_hour_sales） */
    List<HourSales> selectHourSales();

    /** 星期销售（result_weekday_sales） */
    List<WeekdaySales> selectWeekdaySales();

    /** 时段×工作日/周末（result_hour_weekday_sales） */
    List<HourTypeSales> selectHourWeekdaySales();

    /** 星期×时段热力图（result_heatmap_sales） */
    List<HeatCell> selectHeatmapSales();

    // ==================== 月度 ====================

    /** 指定月份的每日销售（result_daily_sales，month 格式 yyyy-MM） */
    List<DailySales> selectDailySales(@Param("month") String month);

    // ==================== 门店 / 城市 ====================

    /** 门店业绩排行（result_store_sales） */
    List<StoreSales> selectStoreSales();

    /** 门店列表（下拉框） */
    List<StoreInfo> selectStores();

    /** 指定门店的月度走势（result_store_month_sales） */
    List<MonthlySales> selectStoreMonthSales(@Param("storeId") Integer storeId);

    /** 指定门店的品类构成（result_store_category_sales） */
    List<CategorySales> selectStoreCategorySales(@Param("storeId") Integer storeId);

    /** 指定门店的热销商品（result_store_product_sales） */
    List<ProductSales> selectStoreProductSales(@Param("storeId") Integer storeId,
                                               @Param("limit") int limit);

    /** 城市消费力（result_city_sales） */
    List<CitySales> selectCitySales();

    /** 城市×月份趋势（result_city_month_sales） */
    List<CityMonthSales> selectCityMonthSales();

    // ==================== 商品 ====================

    /** 全部商品销售（result_product_sales，39 行，前端自行筛选排序） */
    List<ProductSales> selectProductSales();

    /** 商品品类列表（去重） */
    List<String> selectProductCategories();
}
