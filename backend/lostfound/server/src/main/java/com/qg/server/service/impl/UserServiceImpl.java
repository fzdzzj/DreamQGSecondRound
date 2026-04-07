package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.constant.MessageConstant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qg.common.constant.UserStatus;
import com.qg.common.exception.*;
import com.qg.common.util.JwtUtil;
import com.qg.common.util.PasswordUtil;
import com.qg.pojo.dto.ChangePasswordDTO;
import com.qg.pojo.dto.LoginDTO;
import com.qg.pojo.dto.RegisterDTO;
import com.qg.pojo.dto.UpdateUserDTO;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.SysUserVO;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.PermissionService;
import com.qg.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    /**
     * 用户数据访问层对象（构造器注入，不可变）
     */
    private final UserDao userDao;

    /**
     * 权限业务层对象（构造器注入，不可变）
     */
    private final PermissionService permissionService;

    /**
     * JWT 工具类
     */
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        String identifier = loginDTO.getIdentifier();
        String password = loginDTO.getPassword();
        log.info("用户发起登录请求，账号={}", identifier);

        SysUser user;
        if (identifier.contains("@")) {
            // 邮箱登录
            user = userDao.selectOne(new QueryWrapper<SysUser>()
                    .eq("email", identifier)
                    .eq("deleted", 0));
        } else {
            // 手机登录
            user = userDao.selectOne(new QueryWrapper<SysUser>()
                    .eq("phone", identifier)
                    .eq("deleted", 0));
        }

        // 1. 校验账号是否存在
        if (user == null) {
            log.warn("登录失败：用户不存在或已删除，账号={}", identifier);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验账号状态
        if (user.getStatus() == null || !UserStatus.ENABLE.equals(user.getStatus())) {
            log.warn("登录失败：用户已被禁用，账号={}", identifier);
            throw new AccountLockedException(MessageConstant.ACCOUNT_DISABLED);
        }

        // 3. 校验密码
        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            log.warn("登录失败：密码错误，账号={}", identifier);
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        log.info("密码校验通过，账号={}", identifier);

        // 4. 查询权限，后续写入 accessToken，用于 RBAC
        Set<String> permissions = permissionService.getPermissionsByRole(user.getRole()).stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        log.info("用户权限加载完成，账号={}，权限数={}", identifier, permissions.size());

        // 5. 生成双 Token
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                permissions
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        // 6. 返回前端需要的登录结果
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());

        data.put("user", userInfo);

        log.info("用户登录成功，账号={}", identifier);
        return data;
    }


    @Override
    public void register(RegisterDTO registerDTO) {
         String username = registerDTO.getUsername();
         String email = registerDTO.getEmail();
         String phone = registerDTO.getPhone();
         
         // 使用MyBatis Plus条件构造器查询是否已存在
         Long count = userDao.selectCount(new QueryWrapper<SysUser>()
                 .eq("username", username)
                 .or()
                 .eq("phone", phone)
                 .or()
                 .eq("email", email));
         
         if(count>0){//用户名或手机号或邮箱已存在
             log.warn("用户名或手机号或邮箱已存在，用户名：{}", username);
             throw new RegisterFailedException(MessageConstant.USERNAME_OR_PHONE_OR_EMAIL_EXISTS);
         }
         
         String password = registerDTO.getPassword();
         log.info("用户发起注册请求，用户名：{}，邮箱：{}，手机号：{}", username, email, phone);
         SysUser user = new SysUser();
         user.setUsername(username);
         user.setEmail(email);
         user.setPhone(phone);
         user.setPasswordHash(PasswordUtil.encrypt(password));
         user.setRole("STUDENT");
         userDao.insert(user);
    }
    @Override
    public SysUserVO getPersonalInfo(Long userId) {
        log.info("获取个人信息，用户ID：{}", userId);
        SysUser user = userDao.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID：{}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        SysUserVO userVO = new SysUserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void updatePersonalInfo(Long userId, UpdateUserDTO updateUserDTO) {
        log.info("更新个人信息，用户ID={}", userId);
        if (updateUserDTO.getEmail() == null
                && updateUserDTO.getPhone() == null
                && updateUserDTO.getNickname() == null
                && updateUserDTO.getAvatar() == null) {
            log.info("更新个人信息跳过，无有效修改字段，用户ID={}", userId);
            return;
        }

        // 1. 校验当前用户是否存在
        SysUser currentUser = userDao.selectById(userId);
        if (currentUser == null) {
            log.warn("更新个人信息失败，用户不存在，用户ID={}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验邮箱是否被其他用户占用
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().isBlank()) {
            Long emailCount = userDao.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getEmail, updateUserDTO.getEmail())
                            .ne(SysUser::getId, userId)
            );
            if (emailCount != null && emailCount > 0) {
                log.warn("更新个人信息失败，邮箱已被占用，userId={}, email={}", userId, updateUserDTO.getEmail());
                throw new BaseException(400, MessageConstant.EMAIL_EXISTS);
            }
        }

        // 3. 校验手机号是否被其他用户占用
        if (updateUserDTO.getPhone() != null && !updateUserDTO.getPhone().isBlank()) {
            Long phoneCount = userDao.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhone, updateUserDTO.getPhone())
                            .ne(SysUser::getId, userId)
            );
            if (phoneCount != null && phoneCount > 0) {
                log.warn("更新个人信息失败，手机号已被占用，userId={}, phone={}", userId, updateUserDTO.getPhone());
                throw new BaseException(400, MessageConstant.PHONE_EXISTS);
            }
        }

        // 4. 构建更新对象
        SysUser user = new SysUser();
        user.setId(userId);
        BeanUtils.copyProperties(updateUserDTO, user);

        // 5. 更新
        userDao.updateById(user);

        log.info("个人信息更新成功，用户ID={}", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        log.info("修改密码开始，userId={}", userId);

        // 1. 查询用户
        SysUser user = userDao.selectById(userId);
        if (user == null) {
            log.warn("修改密码失败，用户不存在，userId={}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();

        // 2. 校验旧密码
        if (!PasswordUtil.matches(oldPassword, user.getPasswordHash())) {
            log.warn("修改密码失败，旧密码错误，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_ERROR);
        }

        // 3. 新密码不能和旧密码相同
        if (PasswordUtil.matches(newPassword, user.getPasswordHash())) {
            log.warn("修改密码失败，新密码与旧密码相同，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_SAME);
        }

        // 4. DTO层已经校验 confirmPassword，但这里再兜底一层更安全
        if (!newPassword.equals(dto.getConfirmPassword())) {
            log.warn("修改密码失败，两次密码不一致，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_CONFIRM_ERROR);
        }

        // 5. 加密新密码
        String encrypted = PasswordUtil.encrypt(newPassword);

        // 6. 更新
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPasswordHash(encrypted);

        userDao.updateById(update);

        log.info("修改密码成功，userId={}", userId);
    }

}