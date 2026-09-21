package com.supermarket.dto;

import lombok.Data;

/**
 * 门店简要信息（门店详情页下拉框用）
 */
@Data
public class StoreInfo {

    /** 门店编号 */
    private Integer storeId;

    /** 门店名称 */
    private String storeName;

    /** 所在城市 */
    private String city;
}
