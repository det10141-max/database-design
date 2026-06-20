package com.library.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * 分页封装
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long pageSize;
}
