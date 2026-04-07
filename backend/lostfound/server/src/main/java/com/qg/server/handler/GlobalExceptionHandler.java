package com.qg.server.handler;

import com.qg.common.constant.MessageConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.JwtException;
import com.qg.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一捕获所有 Controller 层异常，返回标准化响应，保护接口安全，便于排查问题
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 从配置文件读取最大上传大小
     */
    @Value("${aliyun.oss.max-file-size}")
    private DataSize maxFileSize;

    /**
     * 处理 JSON 请求体参数校验异常（@RequestBody + @Valid 失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理表单参数/路径参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBusinessException(BaseException e, HttpServletResponse response) {
        log.warn("业务异常：{}", e.getMessage(), e);
        // 设置 HTTP 状态码
        if (e.getCode() != null) {
            response.setStatus(e.getCode());
        } else {
            response.setStatus(500);
        }
        return Result.error(e.getCode(), e.getMessage());
    }
    /**
     * 处理SQL异常
     * @param ex
     * @return
     */@ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }


    /**
     * 处理 JWT 认证异常（未登录、token过期、无效）
     */
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwtException(JwtException e) {
        log.warn("JWT异常：{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件上传大小超限：", e);
        String msg = "文件大小不能超过 " + maxFileSize.toMegabytes() + "MB";
        return Result.error(400, msg);
    }

    /**
     * 兜底：处理所有未捕获的系统异常
     * 避免暴露敏感信息，记录完整异常栈
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("服务器未知异常：", e); // 必须输出完整堆栈
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}