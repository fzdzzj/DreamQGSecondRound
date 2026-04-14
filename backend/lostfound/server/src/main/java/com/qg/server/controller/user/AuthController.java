package com.qg.server.controller.user;

import com.qg.common.result.Result;
import com.qg.pojo.dto.LoginDTO;
import com.qg.pojo.dto.RegisterDTO;
import com.qg.pojo.vo.LoginResponseVO;
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

/**
 * 认证接口
 * 提供用户登录、注册、刷新 accessToken 等认证相关的接口
 */
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
     * <p>
     * 返回：
     * - accessToken：访问业务接口
     * - refreshToken：刷新 accessToken
     * - user：当前登录用户基础信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponseVO> login(@Validated @RequestBody LoginDTO loginDTO,
                                         HttpServletRequest request) {
        log.info("用户登录尝试，账号={}", loginDTO.getIdentifier());
        LoginResponseVO response = userService.login(loginDTO, request);
        log.info("用户登录成功，账号={}", loginDTO.getIdentifier());
        return Result.success(response);
    }

    /**
     * 用户注册
     * <p>
     * 参数：
     * - identifier：账号（用户名或手机号）
     * - username：用户名
     * - password：密码
     * - confirmPassword：确认密码
     * - email：邮箱
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册，账号={}", registerDTO.getUsername());
        userService.register(registerDTO);
        log.info("用户注册成功，账号={}", registerDTO.getUsername());
        return Result.success();
    }

    /**
     * 刷新 AccessToken
     * <p>
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
     * <p>
     * 处理逻辑：
     * - 把当前 accessToken 拉入 Redis 黑名单
     * - 把当前 refreshToken 拉入 Redis 黑名单
     * <p>
     * 这样即使客户端本地仍保留旧 token，服务端也会拒绝它继续访问。
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(HttpServletRequest request,
                               @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        log.info("用户退出登录尝试");
        String authHeader = request.getHeader("Authorization");
        String accessToken = extractBearerToken(authHeader);
        log.warn("用户退出登录尝试，未携带 accessToken");
        tokenRefreshService.addTokenToBlacklist(accessToken);
        tokenRefreshService.addTokenToBlacklist(refreshToken);

        log.info("用户退出登录成功，accessToken已拉黑，refreshToken已拉黑");

        return Result.success();
    }

    /**
     * 从 Authorization: Bearer xxx 中提取 token
     *
     * @param authHeader Authorization 头
     *                   格式为：Bearer xxx
     * @return 提取到的 token，或 null 如果格式错误
     */
    private String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        log.info("Authorization 头：{}", authHeader);
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }

}
