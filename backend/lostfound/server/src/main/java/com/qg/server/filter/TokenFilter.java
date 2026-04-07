package com.qg.server.filter;

import com.qg.common.context.BaseContext;
import com.qg.common.util.JwtUtil;
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
 * Token 认证过滤器
 *
 * 设计目标：
 * 1. 对需要登录的接口做统一认证
 * 2. 解析 JWT，恢复当前登录用户上下文
 * 3. 基于 JWT 中携带的 permissions 做 RBAC 权限校验
 * 4. 结合 Redis 黑名单，让登出、改密后的旧 token 立即失效
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRefreshService tokenRefreshService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 开发期开关：
     * true  = 启用认证过滤
     * false = 整个过滤器直接跳过，方便前期调接口
     */
    @Value("${auth.filter.enabled:true}")
    private boolean filterEnabled;

    /**
     * 开发期白名单。
     * 这些接口不做 token 校验，便于登录、文档调试、列表浏览。
     */
    private static final List<String> WHITE_LIST = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error",
            "/api/file/upload",
            "/item/page",
            "/item/*"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!filterEnabled) {
            return true;
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        try {
            // 1. 读取 Bearer Token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("认证失败，未携带Token，{} {}", method, uri);
                writeJson(response, 401, "未登录，请先登录");
                return;
            }

            String token = authHeader.substring(7).trim();

            // 2. Redis 黑名单校验
            if (tokenRefreshService.isBlacklisted(token)) {
                log.warn("认证失败，Token已在黑名单中，{} {}", method, uri);
                writeJson(response, 401, "Token已失效，请重新登录");
                return;
            }

            // 3. 解析 Token
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null) {
                log.warn("认证失败，Token解析失败，{} {}", method, uri);
                writeJson(response, 401, "令牌不合法");
                return;
            }

            // 4. 必须是 accessToken，refreshToken 不能访问业务接口
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("认证失败，禁止使用 refreshToken 访问业务接口，{} {}", method, uri);
                writeJson(response, 401, "Token类型错误");
                return;
            }

            // 5. 读取用户信息
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            @SuppressWarnings("unchecked")
            Set<String> permissions = claims.get("permissions", Set.class);

            if (userId == null || username == null || role == null) {
                log.warn("认证失败，Token信息不完整，{} {}", method, uri);
                writeJson(response, 401, "Token信息不完整");
                return;
            }

            // 6. 写入线程上下文，供后续 Controller / Service 使用
            BaseContext.setCurrentUser(userId, role);


            // 7. RBAC 权限校验
            if (!hasPermission(uri, method, role, permissions)) {
                log.warn("权限不足，userId={}, role={}, {} {}", userId, role, method, uri);
                writeJson(response, 403, "权限不足");
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            BaseContext.remove();
        }
    }

    /**
     * RBAC 权限校验
     *
     * 规则：
     * - ADMIN / SYSTEM 直接放行
     * - STUDENT 根据 permissions 精确匹配 METHOD:PATH
     */
    private boolean hasPermission(String uri, String method, String role, Set<String> permissions) {
        if ("ADMIN".equals(role) || "SYSTEM".equals(role)) {
            return true;
        }

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        String normalizedUri = normalizeUri(uri);
        String currentPerm = (method + ":" + normalizedUri).trim();

        return permissions.stream()
                .map(String::trim)
                .anyMatch(currentPerm::equals);
    }

    /**
     * 把带数字 ID 的路径标准化，避免权限匹配失败
     * 例如：
     * /item/123 -> /item/{id}
     */
    private String normalizeUri(String uri) {
        if (uri == null) {
            return "";
        }
        return uri.replaceAll("/\\d+", "/{id}");
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code == 401 ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", code, msg));
    }
}
