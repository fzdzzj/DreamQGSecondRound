package com.qg.common.result;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * 统一返回结果类
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    private Boolean success;
    private LocalDateTime timestamp;
    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "操作成功";
        r.data = data;
        r.success = true;
        r.timestamp = LocalDateTime.now();
        return r;
    }
    /**
     * 成功返回结果
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    /**
     * 失败返回结果
     *
     * @param code 状态码
     * @param message 错误信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.success = false;
        r.timestamp = LocalDateTime.now();
        return r;
    }
    /**
     * 失败返回结果
     * @param message 错误信息
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
