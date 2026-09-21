package com.supermarket.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 结果表数据浏览 Mapper（表名由 Service 层白名单校验后传入）
 */
public interface DataMapper {

    /** 查询指定结果表的分页数据 */
    List<Map<String, Object>> selectRows(@Param("table") String table,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    /** 指定结果表的总行数 */
    long countRows(@Param("table") String table);
}
