package com.qg.server.config;

import com.qg.common.constant.JwtClaimsConstant;
import com.qg.common.constant.RoleConstant;
import com.qg.common.constant.TokenConstant;
import com.qg.common.constant.UserStatusConstant;
import com.qg.common.util.JwtUtil;
import com.qg.pojo.entity.SysUser;
import com.qg.server.mapper.UserDao;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;

/**
 * WebSocket 握手拦截器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDao userDao;
    /**
     * 握手前
     * 1. 解析token
     * 2. 校验token
     * 3. 校验用户是否存在
     * 4. 校验用户是否有访问权限
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        //1. 解析token
        String token = null;
        try {
            String auth = request.getHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }

            if (token == null || token.isBlank()) {
                MultiValueMap<String, String> queryParams =
                        UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
                token = queryParams.getFirst("token");
            }

            if (token == null || token.isBlank()) {
                log.warn("WebSocket握手失败：未携带token");
                return false;
            }

            Claims claims = jwtUtil.parseToken(token);

            String tokenType = claims.get("type", String.class);
            if (!TokenConstant.ACCESS.equals(tokenType)) {
                log.warn("WebSocket握手失败：token类型错误");
                return false;
            }
            //2. 校验token
            Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
            String username = claims.get(JwtClaimsConstant.USERNAME, String.class);
            String role = claims.get(JwtClaimsConstant.ROLE, String.class);
            Set<String> permissions = jwtUtil.getPermissionsFromToken(claims);

            if (userId == null || username == null || role == null) {
                log.warn("WebSocket握手失败：token信息不完整");
                return false;
            }
            //3. 校验用户是否存在
            SysUser user = userDao.selectById(userId);
            if (user == null || UserStatusConstant.DISABLE.equals(user.getStatus())) {
                log.warn("WebSocket握手失败：用户不存在或已禁用, userId={}", userId);
                return false;
            }

            // 如果你要做权限判断，可以在这里校验用户是否有访问权限
            String uri = request.getURI().getPath();
            boolean hasPermission = permissions != null && permissions.stream().anyMatch(permission -> {
                if (permission == null || permission.isBlank()) {
                    return false;
                }

                String[] arr = permission.split(":", 2);
                if (arr.length != 2) {
                    return false;
                }

                String permMethod = arr[0].trim();
                String permUri = arr[1].trim();

                // WebSocket 握手本质是 GET
                return "GET".equalsIgnoreCase(permMethod) && new AntPathMatcher().match(permUri, uri);
            });

            if (!RoleConstant.SYSTEM.equals(role) && !hasPermission) {
                log.warn("WebSocket握手失败：无访问权限, userId={}, uri={}", userId, uri);
                return false;
            }
            //4. 校验通过，将用户信息添加到attributes中
            attributes.put("userId", userId);
            attributes.put("username", username);
            attributes.put("role", role);

            log.info("WebSocket握手成功, userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手异常", e);
            return false;
        }
    }


    /**
     * 握手完成后
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手完成后出现异常, uri={}", request.getURI(), exception);
        }
    }

}
