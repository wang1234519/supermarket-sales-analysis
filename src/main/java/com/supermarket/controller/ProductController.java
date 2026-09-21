package com.supermarket.controller;

import com.supermarket.dto.ProductSales;
import com.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分析接口（读取 Hive 计算结果表）
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 全部商品销售结果（39 行，筛选/排序/畅销/滞销由前端处理） */
    @GetMapping("/list")
    public List<ProductSales> list() {
        return productService.getList();
    }

    /** 商品品类列表（筛选下拉框） */
    @GetMapping("/categories")
    public List<String> categories() {
        return productService.getCategories();
    }
}
