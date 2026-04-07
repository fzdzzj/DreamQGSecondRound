package com.qg.server.service;

import com.qg.pojo.dto.ChangePasswordDTO;
import com.qg.pojo.dto.LoginDTO;
import com.qg.pojo.dto.RegisterDTO;
import com.qg.pojo.dto.UpdateUserDTO;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.SysUserVO;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);

    /**
     * 获取个人信息
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUserVO getPersonalInfo(Long userId);

    /**
     * 更新个人信息
     * @param userId 用户ID
     * @param updateUserDTO 用户信息
     */
    void updatePersonalInfo(Long userId, UpdateUserDTO updateUserDTO);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码DTO
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}
