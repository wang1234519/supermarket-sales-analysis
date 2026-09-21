package com.supermarket.controller;

import com.supermarket.dto.CategoryMonthSales;
import com.supermarket.dto.CategorySales;
import com.supermarket.dto.ProductSales;
import com.supermarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 品类分析接口
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 品类销售排行（含占比） */
    @GetMapping("/overview")
    public List<CategorySales> overview() {
        return categoryService.getOverview();
    }

    /** 品类 × 月份 趋势（多系列折线图） */
    @GetMapping("/trend")
    public List<CategoryMonthSales> trend() {
        return categoryService.getTrend();
    }

    /** 指定品类的商品销售排行 */
    @GetMapping("/top-products")
    public List<ProductSales> topProducts(@RequestParam String category) {
        return categoryService.getTopProducts(category);
    }

    /** 全部品类列表（筛选下拉框） */
    @GetMapping("/categories")
    public List<String> categories() {
        return categoryService.getCategories();
    }
}
