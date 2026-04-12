package com.qg.server.handler;

import com.qg.common.constant.MessageConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.JwtException;
import com.qg.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${aliyun.oss.max-file-size}")
    private DataSize maxFileSize;

    private static final String DEFAULT_ERROR = "系统繁忙，请稍后重试";

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException manv) {
            message = manv.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ":" + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
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
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(400,"请求体不能为空或格式错误");
    }


    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBusiness(BaseException e, HttpServletResponse response) {
        log.warn("业务异常：{}", e.getMessage(), e);
        response.setStatus(e.getCode() != null ? e.getCode() : 500);
        return Result.error(e.getCode(), e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSql(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库约束异常：", ex);
        String msg = ex.getMessage() != null && ex.getMessage().contains("Duplicate entry") ?
                ex.getMessage().split(" ")[2] + MessageConstant.ALREADY_EXISTS :
                DEFAULT_ERROR;
        return Result.error(msg);
    }

    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwt(JwtException e) {
        log.warn("JWT异常：{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage() != null ? e.getMessage() : "Token验证失败");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleFile(MaxUploadSizeExceededException e) {
        log.error("文件上传大小超限：", e);
        return Result.error(400, "文件大小不能超过 " + maxFileSize.toMegabytes() + "MB");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("服务器未知异常：", e);
        return Result.error(500, DEFAULT_ERROR);
    }
}
