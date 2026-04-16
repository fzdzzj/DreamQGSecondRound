package com.qg.server.filter;

import com.qg.common.constant.JwtClaimsConstant;
import com.qg.common.constant.RoleConstant;
import com.qg.common.constant.TokenConstant;
import com.qg.common.constant.UserStatusConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.common.util.JwtUtil;
import com.qg.common.util.ResponseUtil;
import com.qg.pojo.entity.SysUser;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.TokenRefreshService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Token 过滤器
 * 用于验证请求中的 Token 并设置上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRefreshService tokenRefreshService;
    private final UserDao userDao;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${auth.filter.enabled:true}")
    private boolean filterEnabled;

    private static final List<String> WHITE_LIST = List.of(
            "/auth/login", "/auth/register", "/auth/refresh",
            "/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs/**", "/error",
            "/api/file/upload", "/item/page",
            "/email/sendCode", "/email/verifyCode"
    );

    /**
     * 忽略过滤的 URL
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!filterEnabled) return true;
        // 允许 OPTIONS 请求通过
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;
        // 忽略白名单中的 URL
        String uri = request.getRequestURI();
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    /**
     * 过滤器逻辑
     * 验证请求中的 Token 并设置上下文
     * 1. Token 检查
     * 2. Token 解析
     * 3. Token 校验
     * 4. 获取权限
     * 5. 设置上下文
     * 6. 验证权限
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        try {
            // 1. Token 检查
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ResponseUtil.write(response, Result.error(401, "未登录，请先登录"));
                return;
            }

            String token = authHeader.substring(7).trim();

            if (tokenRefreshService.isBlacklisted(token)) {
                ResponseUtil.write(response, Result.error(401, "Token已失效，请重新登录"));
                return;
            }

            Claims claims;
            try {
                // 2. Token 解析
                claims = jwtUtil.parseToken(token);
                log.info("Token 解析成功：{}", claims);
            } catch (Exception e) {
                log.warn("Token 解析失败：{}", e.getMessage());
                ResponseUtil.write(response, Result.error(401, "Token无效或已过期"));
                return;
            }

            String tokenType = claims.get("type", String.class);
            if (!TokenConstant.ACCESS.equals(tokenType)) {
                ResponseUtil.write(response, Result.error(401, "Token类型错误"));
                return;
            }
            // 3. Token 校验
            Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
            String username = claims.get(JwtClaimsConstant.USERNAME, String.class);
            String role = claims.get(JwtClaimsConstant.ROLE, String.class);
            // 4. 获取权限
            Set<String> permissions = jwtUtil.getPermissionsFromToken(claims);
            log.info("权限列表大小：{}", permissions.size());


            if (userId == null || username == null || role == null) {
                ResponseUtil.write(response, Result.error(401, "Token信息不完整"));
                return;
            }
            // 5. 设置上下文
            BaseContext.setCurrentUser(userId, role);
            // 6.1 验证权限
            if (RoleConstant.ADMIN.equals(role) || RoleConstant.USER.equals(role)) {
                SysUser user = userDao.selectById(userId);
                if (UserStatusConstant.DISABLE.equals(user.getStatus())) {
                    ResponseUtil.write(response, Result.error(403, "用户已被禁用"));
                    return;
                }
            }

            // 6.2 RBAC 权限校验
            if (!hasPermission(uri, method, role, permissions)) {
                log.warn("用户: {} 无权限访问: {}", username, uri);
                ResponseUtil.write(response, Result.error(403, "权限不足"));
                return;
            }
            filterChain.doFilter(request, response);

        } finally {
            BaseContext.remove();
        }
    }

    private boolean hasPermission(String uri, String method, String role, Set<String> permissions) {
        if (RoleConstant.SYSTEM.equals(role)) {
            return true;
        }

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        log.info("当前请求：{} {}", method, uri);
        log.info("权限列表：{}", permissions);


        return permissions.stream().anyMatch(permission -> {
            if (permission == null || permission.isBlank()) {
                return false;
            }

            String[] arr = permission.split(":", 2);
            if (arr.length != 2) {
                return false;
            }

            String permMethod = arr[0].trim();
            String permUri = arr[1].trim();

            if (!permMethod.equalsIgnoreCase(method)) {
                return false;
            }

            return pathMatcher.match(permUri, uri);
        });
    }
}
