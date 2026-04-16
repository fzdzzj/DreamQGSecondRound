package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.pojo.dto.*;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.LoginResponseVO;
import com.qg.pojo.vo.SysUserDetailVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 */
public interface UserService extends IService<SysUser> {

    /**
     * 用户登录，验证用户身份，返回登录信息（Token 和用户信息）
     *
     * @param loginDTO 登录请求的DTO
     * @return 登录后的用户信息和 token
     */
    LoginResponseVO login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 用户注册，完成用户信息的注册操作
     *
     * @param registerDTO 用户注册的DTO
     */
    void register(RegisterDTO registerDTO);

    /**
     * 获取用户个人信息
     *
     * @param userId 用户ID
     * @return 用户个人信息的VO对象
     */
    SysUserDetailVO getPersonalInfo(Long userId);

    /**
     * 更新用户个人信息
     *
     * @param userId        用户ID
     * @param updateUserDTO 更新的用户信息
     */
    void updatePersonalInfo(Long userId, UpdateUserDTO updateUserDTO);

    /**
     * 修改用户密码
     *
     * @param userId            用户ID
     * @param changePasswordDTO 新密码的DTO
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);

    /**
     * 通过验证码修改密码
     *
     * @param dto    验证码和密码的DTO
     */
    void changePasswordByCode(ChangePasswordByCodeDTO dto);

}
