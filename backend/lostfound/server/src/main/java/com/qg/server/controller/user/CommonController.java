package com.qg.server.controller.user;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.dto.ChangePasswordByCodeDTO;
import com.qg.pojo.dto.ChangePasswordDTO;
import com.qg.pojo.dto.UpdateUserDTO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.server.service.TokenRefreshService;
import com.qg.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口
 * 提供用户个人信息、修改密码等操作的接口
 */
@RestController
@RequestMapping("/common")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "个人中心接口")
public class CommonController {

    private final TokenRefreshService tokenRefreshService;

    private final UserService userService;

    /**
     * 获取个人信息
     *
     * @return 个人信息的VO对象
     */
    @GetMapping("/personal-info")
    @Operation(summary = "获取个人信息")
    public Result<SysUserDetailVO> getPersonalInfo() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取个人信息，用户ID={}", userId);
        SysUserDetailVO userInfo = userService.getPersonalInfo(userId);
        log.info("个人信息获取成功，用户ID={}", userId);
        return Result.success(userInfo);
    }

    /**
     * 更新个人信息
     *
     * @param updateUserDTO 更新的用户信息
     *                      包含昵称、邮箱、手机号等字段
     * @return 更新结果
     */
    @PutMapping("/personal-info")
    @Operation(summary = "更新个人信息")
    public Result<Void> updatePersonalInfo(@Validated @RequestBody UpdateUserDTO updateUserDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("更新个人信息，用户ID={}", userId);
        userService.updatePersonalInfo(userId, updateUserDTO);
        log.info("个人信息更新成功，用户ID={}", userId);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * @param changePasswordDTO 新密码的DTO
     * @return 修改密码结果
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Validated @RequestBody ChangePasswordDTO changePasswordDTO,
                                       HttpServletRequest request,
                                       @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改密码，用户ID={}", userId);

        userService.changePassword(userId, changePasswordDTO);

        // 修改密码成功后，让当前 token 立即失效，强制重新登录
        String authHeader = request.getHeader("Authorization");
        String accessToken = extractBearerToken(authHeader);

        tokenRefreshService.addTokenToBlacklist(accessToken);
        tokenRefreshService.addTokenToBlacklist(refreshToken);

        log.info("密码修改成功，旧 token 已失效，用户ID={}", userId);
        return Result.success();
    }

    /**
     * 验证码修改密码
     *
     * @param dto 验证码修改密码的DTO
     * @return 修改密码结果
     */
    @PutMapping("/password/by-code")
    @Operation(summary = "验证码修改密码（无需旧密码）")
    public Result<Void> changePasswordByCode(@Validated @RequestBody ChangePasswordByCodeDTO dto,
                                             HttpServletRequest request,
                                             @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        Long userId = BaseContext.getCurrentId();
        log.info("验证码修改密码，用户ID={}", userId);

        // 核心：验证码修改密码（内部校验邮箱、验证码、类型）
        userService.changePasswordByCode(userId, dto);

        // 修改密码后强制重新登录
        String authHeader = request.getHeader("Authorization");
        String accessToken = extractBearerToken(authHeader);
        tokenRefreshService.addTokenToBlacklist(accessToken);
        tokenRefreshService.addTokenToBlacklist(refreshToken);

        log.info("验证码修改密码成功，token已拉黑，用户ID={}", userId);
        return Result.success();
    }

    /**
     * 从 Authorization 头中提取 Bearer token
     *
     * @param authHeader Authorization 头
     * @return Bearer token
     */
    private String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        log.info("从 Authorization 头中提取 Bearer token，Authorization 头：{}", authHeader);
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }

}
