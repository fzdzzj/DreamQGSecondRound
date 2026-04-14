package com.qg.server.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 监测service方法的性能
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    /**
     * 监控 Service 层所有方法
     */
    @Around("execution(* com.qg.server.service..*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        // 方法名
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;

            // 超过 2 秒的方法记录警告
            if (costTime > 2000) {
                log.warn(" 方法执行缓慢：{} | 耗时：{}ms", methodName, costTime);
            } else {
                log.debug("✓ 方法执行：{} | 耗时：{}ms", methodName, costTime);
            }

            return result;
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("✗ 方法执行异常：{} | 耗时：{}ms | 错误：{}",
                    methodName, costTime, e.getMessage(),e);
            throw e;
        }
    }
}