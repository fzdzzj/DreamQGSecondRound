package com.qg.server.handler;

import com.qg.common.constant.MessageConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.JwtException;
import com.qg.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 * 用于处理应用中的异常情况，如参数校验失败、业务异常、数据库约束异常等
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${aliyun.oss.max-file-size}")
    private DataSize maxFileSize;

    private static final String DEFAULT_ERROR = "系统繁忙，请稍后重试";

    /**
     * 类型转换异常
     *
     * @param e 异常对象
     * @return 错误结果
     */
    @ExceptionHandler(TypeMismatchException.class)
    public Result<?> handleTypeMismatch(TypeMismatchException e) {
        log.warn("类型转换异常：{}", e.getMessage());
        return Result.error(400, "参数类型错误，请检查输入格式");
    }
    /**
     * 参数校验失败
     *
     * @param e 异常对象
     * @return 错误结果
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception e) {
        String message;
        // 处理 MethodArgumentNotValidException 异常
        if (e instanceof MethodArgumentNotValidException manv) {
            message = manv.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ":" + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            // 处理 BindException 异常
        } else if (e instanceof BindException be) {
            message = be.getFieldErrors().stream()
                    .map(err -> err.getField() + ":" + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else {
            message = "参数错误";
        }
        log.warn("参数校验失败：{}", message);
        return Result.error(400, message);
    }

    /**
     * 请求体不能为空或格式错误
     *
     * @return 错误结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleBodyError() {
        return Result.error(400, "请求体格式错误");
    }

    /**
     * 业务异常
     *
     * @param e        异常对象
     * @param response 响应对象
     * @return 错误结果
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBusiness(BaseException e, HttpServletResponse response) {
        log.warn("业务异常：{}", e.getMessage(), e);
        response.setStatus(e.getCode() != null ? e.getCode() : 500);
        return Result.error(e.getCode(), e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR);
    }

    /**
     * 数据库约束异常
     *
     * @param ex 异常对象
     * @return 错误结果
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSql(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库约束异常：", ex);
        // 判断是否是重复数据异常
        String msg = ex.getMessage() != null && ex.getMessage().contains("Duplicate entry") ?
                ex.getMessage().split(" ")[2] + MessageConstant.ALREADY_EXISTS :
                DEFAULT_ERROR;
        return Result.error(msg);
    }

    /**
     * JWT 异常
     *
     * @param e 异常对象
     * @return 错误结果
     */
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwt(JwtException e) {
        log.warn("JWT异常：{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage() != null ? e.getMessage() : "Token验证失败");
    }

    /**
     * 文件上传大小超限
     *
     * @param e 异常对象
     * @return 错误结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleFile(MaxUploadSizeExceededException e) {
        log.error("文件上传大小超限：", e);
        return Result.error(400, "文件大小不能超过 " + maxFileSize.toMegabytes() + "MB");
    }

    /**
     * 未知异常
     *
     * @param e 异常对象
     * @return 错误结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("服务器未知异常：", e);
        return Result.error(500, DEFAULT_ERROR);
    }

    /**
     * 运行时异常
     *
     * @param e 运行时异常对象
     * @return 错误结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}
