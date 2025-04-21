package com.fq.yznu.eaps.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页工具类
 */
public class PageUtils {

    /**
     * 将MyBatis-Plus分页结果转换为通用分页结果
     *
     * @param page MyBatis-Plus分页结果
     * @param <T>  数据类型
     * @return 通用分页结果
     */
    public static <T> Map<String, Object> getPageMap(IPage<T> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());
        return result;
    }

    /**
     * 创建MyBatis-Plus分页对象
     *
     * @param params 包含分页参数的Map
     * @return MyBatis-Plus分页对象
     */
    public static <T> Page<T> getPage(Map<String, Object> params) {
        long current = 1;
        long size = 10;
        
        if (params.get("page") != null) {
            current = Long.parseLong(params.get("page").toString());
        }
        if (params.get("limit") != null) {
            size = Long.parseLong(params.get("limit").toString());
        }
        
        return new Page<>(current, size);
    }

    /**
     * 构建包含分页信息的结果Map
     *
     * @param list    数据列表
     * @param total   总记录数
     * @param current 当前页
     * @param size    每页大小
     * @return 结果Map
     */
    public static Map<String, Object> buildPageResult(List<?> list, long total, long current, long size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", current);
        result.put("size", size);
        result.put("pages", total % size == 0 ? total / size : total / size + 1);
        return result;
    }
} 