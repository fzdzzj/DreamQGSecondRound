package com.qg.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * XSS过滤器
 */
@Component
@Slf4j
public class XssFilter extends OncePerRequestFilter {
    /**
     * 过滤请求
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("XSS过滤器开始处理请求");
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
        log.info("XSS过滤器处理请求完成");
    }
}
