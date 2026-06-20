package com.library.enums;

/**
 * 预约状态枚举
 */
public enum ReserveStatusEnum {
    WAITING,     // 等待中
    FULFILLED,   // 已到书待取
    CANCELLED,   // 已取消
    EXPIRED,     // 已过期
    BORROWED     // 已取书借阅
}
