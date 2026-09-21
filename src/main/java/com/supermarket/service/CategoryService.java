package com.supermarket.service;

import com.supermarket.dto.CategoryMonthSales;
import com.supermarket.dto.CategorySales;
import com.supermarket.dto.ProductSales;
import com.supermarket.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 品类分析服务（只读 Hive 计算结果表）
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ResultMapper resultMapper;

    /** 品类销售排行（占比已在 Hive 中算好） */
    public List<CategorySales> getOverview() {
        return resultMapper.selectCategorySales();
    }

    /** 品类 × 月份 趋势 */
    public List<CategoryMonthSales> getTrend() {
        return resultMapper.selectCategoryMonthSales();
    }

    /** 指定品类的商品销售排行 */
    public List<ProductSales> getTopProducts(String category) {
        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException("品类不能为空");
        }
        return resultMapper.selectCategoryProducts(category.trim(), 10);
    }

    /** 全部品类列表（筛选下拉框） */
    public List<String> getCategories() {
        return resultMapper.selectProductCategories();
    }
}
