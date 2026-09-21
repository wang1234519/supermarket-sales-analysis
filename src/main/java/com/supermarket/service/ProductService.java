package com.supermarket.service;

import com.supermarket.dto.ProductSales;
import com.supermarket.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分析服务（只读 Hive 计算结果表）
 *
 * 结果表仅 39 行，畅销/滞销/搜索/排序均由前端对全量列表处理，后端不再分页计算。
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ResultMapper resultMapper;

    /** 全部商品销售结果（按销售额降序） */
    public List<ProductSales> getList() {
        return resultMapper.selectProductSales();
    }

    /** 商品品类列表 */
    public List<String> getCategories() {
        return resultMapper.selectProductCategories();
    }
}
