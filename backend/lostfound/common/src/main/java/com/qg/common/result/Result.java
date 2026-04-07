package com.qg.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /**
     * 返回成功结果
     *
     * @param data 数据
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 返回成功结果
     *
     * @param <T>
     * @return
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 返回失败结果
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 返回失败结果
     *
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}