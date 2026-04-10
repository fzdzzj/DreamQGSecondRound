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

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRefreshService tokenRefreshService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${auth.filter.enabled:true}")
    private boolean filterEnabled;

    private static final List<String> WHITE_LIST = List.of(
            "/auth/login", "/auth/register", "/auth/refresh",
            "/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs/**", "/error",
            "/api/file/upload", "/item/page", "/item/*"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!filterEnabled) return true;
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;
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
            // 1. Token 检查
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                writeJson(response, 401, "未登录，请先登录");
                return;
            }

            String token = authHeader.substring(7).trim();

            if (tokenRefreshService.isBlacklisted(token)) {
                writeJson(response, 401, "Token已失效，请重新登录");
                return;
            }

            Claims claims = jwtUtil.parseToken(token);
            if (claims == null) {
                writeJson(response, 401, "令牌不合法");
                return;
            }

            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                writeJson(response, 401, "Token类型错误");
                return;
            }

            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            @SuppressWarnings("unchecked")
            Set<String> permissions = jwtUtil.getPermissionsFromToken(claims);
            log.warn("permissions: {}", permissions);
            if (userId == null || username == null || role == null) {
                writeJson(response, 401, "Token信息不完整");
                return;
            }

            BaseContext.setCurrentUser(userId, role);
            if(!tokenRefreshService.isBlacklisted(token)){

            // 2. RBAC 权限校验
            if (!hasPermission(uri, method, role, permissions)) {
                log.warn("用户: " + username + " 无权限访问: " + uri);
                writeJson(response, 403, "权限不足");
                return;
            }
            if(uri.equals("/auth/refresh")){
                tokenRefreshService.addTokenToBlacklist(token);
            }
                filterChain.doFilter(request, response);
            }



        } finally {
            BaseContext.remove();
        }
    }

    private boolean hasPermission(String uri, String method, String role, Set<String> permissions) {
        if ("ADMIN".equals(role) || "SYSTEM".equals(role)) return true;
        if (permissions == null || permissions.isEmpty()) return false;

        String normalizedUri = uri.replaceAll("/\\d+", "/{id}");
        String currentPerm = (method + ":" + normalizedUri).trim();
        log.warn("当前权限：" + currentPerm);
        log.warn("权限列表：" + permissions);

        return permissions.stream().anyMatch(p -> p.trim().equals(currentPerm));
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code == 401 ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", code, msg));
    }
}
