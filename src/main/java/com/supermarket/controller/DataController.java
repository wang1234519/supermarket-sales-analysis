package com.supermarket.controller;

import com.supermarket.dto.PageResult;
import com.supermarket.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 结果表数据浏览接口（只读）
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    /** 结果表清单 */
    @GetMapping("/tables")
    public List<Map<String, String>> tables() {
        return dataService.getTables();
    }

    /** 指定结果表的分页数据 */
    @GetMapping("/{table}")
    public PageResult<Map<String, Object>> data(@PathVariable String table,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "15") int size) {
        return dataService.getData(table, page, size);
    }
}
