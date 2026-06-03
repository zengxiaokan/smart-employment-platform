package com.itzk.SmartEmploymentPlatform.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;

import java.util.function.Supplier;

/**
 * 分页工具 —— 封装 PageHelper，统一返回 PageResult
 *
 * 使用示例：
 *   PageResult result = PageUtil.pageQuery(1, 10, () -> mapper.listXxx(dto));
 */
public final class PageUtil {

    private PageUtil() {}

    public static <T> PageResult pageQuery(int pageNum, int pageSize, Supplier<?> query) {
        PageHelper.startPage(pageNum, pageSize);
        Page<T> page = (Page<T>) query.get();
        return new PageResult(page.getTotal(), page.getResult());
    }
}
