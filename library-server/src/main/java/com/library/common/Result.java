package com.library.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体 { code, msg, data }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "成功", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, "成功", null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(400, msg, null);
    }

    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }

    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null);
    }
}
