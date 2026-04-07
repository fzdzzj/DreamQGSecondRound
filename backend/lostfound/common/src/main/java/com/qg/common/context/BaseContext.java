package com.qg.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基于ThreadLocal封装工具类，用于保存和获取当前登录用户信息
 */
public class BaseContext {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserContext {
        private Long id;      // 用户ID
        private String role;  // 用户角色 student/admin
    }

    // 线程上下文，存 UserContext
    private static final ThreadLocal<UserContext> threadLocal = new ThreadLocal<>();

    // 设置当前用户信息
    public static void setCurrentUser(Long id, String role) {
        threadLocal.set(new UserContext(id, role));
    }

    // 获取当前用户ID
    public static Long getCurrentId() {
        UserContext context = threadLocal.get();
        return context == null ? null : context.getId();
    }

    // 获取当前用户角色
    public static String getCurrentRole() {
        UserContext context = threadLocal.get();
        return context == null ? null : context.getRole();
    }

    // 清理
    public static void remove() {
        threadLocal.remove();
    }
}
