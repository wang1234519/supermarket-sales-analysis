package com.supermarket.dto;

import lombok.Data;

import java.util.List;

/**
 * 门店详情：经营概况 + 月度走势 + 品类构成 + 热销商品
 */
@Data
public class StoreDetail {

    /** 门店经营概况 */
    private StoreSales overview;

    /** 月度销售走势 */
    private List<MonthlySales> monthly;

    /** 品类销售构成 */
    private List<CategorySales> categories;

    /** 热销商品 TOP10 */
    private List<ProductSales> topProducts;
}
