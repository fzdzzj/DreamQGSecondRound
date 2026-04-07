package com.qg.server.controller;

import com.qg.common.result.Result;
import com.qg.pojo.dto.LoginDTO;
import com.qg.pojo.dto.RegisterDTO;
import com.qg.server.anno.OperationLog;
import com.qg.server.service.TokenRefreshService;
import com.qg.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口")
public class AuthController {

    private final UserService userService;
    private final TokenRefreshService tokenRefreshService;

    /**
     * 用户登录
     *
     * 返回：
     * - accessToken：访问业务接口
     * - refreshToken：刷新 accessToken
     * - user：当前登录用户基础信息
     */
    @PostMapping("/login")
    @OperationLog("用户登录")
    @Operation(summary = "用户登录", description = "用户登录，返回用户信息及 token")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        log.info("用户登录尝试，账号={}", loginDTO.getIdentifier());
        Map<String, Object> response = userService.login(loginDTO);
        log.info("用户登录成功，账号={}", loginDTO.getIdentifier());
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @OperationLog("用户注册")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册，账号={}", registerDTO.getUsername());
        userService.register(registerDTO);
        log.info("用户注册成功，账号={}", registerDTO.getUsername());
        return Result.success();
    }

    /**
     * 刷新 AccessToken
     *
     * 前端在 accessToken 过期后，携带 refreshToken 调这个接口，
     * 成功后拿到新的 accessToken，再重放原请求。
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public Result<Map<String, String>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("开始刷新 accessToken");
        Map<String, String> tokens = tokenRefreshService.refreshTokens(refreshToken);
        log.info("刷新 accessToken 成功");
        return Result.success(tokens);
    }

    /**
     * 退出登录
     *
     * 处理逻辑：
     * - 把当前 accessToken 拉入 Redis 黑名单
     * - 把当前 refreshToken 拉入 Redis 黑名单
     *
     * 这样即使客户端本地仍保留旧 token，服务端也会拒绝它继续访问。
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(HttpServletRequest request,
                               @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {

        String authHeader = request.getHeader("Authorization");
        String accessToken = extractBearerToken(authHeader);

        tokenRefreshService.addTokenToBlacklist(accessToken);
        tokenRefreshService.addTokenToBlacklist(refreshToken);

        log.info("用户退出登录成功，accessToken已拉黑，refreshToken已拉黑");

        return Result.success();
    }

    /**
     * 从 Authorization: Bearer xxx 中提取 token
     */
    private String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }
}
