package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.UserStatus;
import com.qg.common.exception.*;
import com.qg.common.util.JwtUtil;
import com.qg.common.util.PasswordUtil;
import com.qg.pojo.dto.*;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.SysUserDetailVO;
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
public class UserServiceImpl extends ServiceImpl<UserDao, SysUser> implements UserService {

    private final UserDao userDao;  // 用户数据访问层
    private final PermissionService permissionService;  // 权限服务层
    private final JwtUtil jwtUtil;  // JWT 工具类

    /**
     * 用户登录操作：验证用户身份，生成访问 Token 和刷新 Token
     *
     * @param loginDTO 登录请求的DTO
     * @return 登录后的 token 和用户信息
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        String identifier = loginDTO.getIdentifier();  // 用户标识（邮箱或手机）
        String password = loginDTO.getPassword();  // 用户密码
        log.info("用户发起登录请求，账号={}", identifier);

        // 根据账号类型（手机号或邮箱）查询用户
        SysUser user = null;
        if (identifier.contains("@")) {
            user = userDao.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, identifier)
                    .eq(SysUser::getDeleted, 0));  // 未删除的用户
        } else {
            user = userDao.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, identifier)
                    .eq(SysUser::getDeleted, 0));  // 未删除的用户
        }

        // 用户不存在或已删除，抛出异常
        if (user == null) {
            log.warn("登录失败：用户不存在或已删除，账号={}", identifier);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验账号状态（账号是否禁用）
        if (user.getStatus() == null || !UserStatus.ENABLE.equals(user.getStatus())) {
            log.warn("登录失败：用户已被禁用，账号={}", identifier);
            throw new AccountLockedException(MessageConstant.ACCOUNT_DISABLED);
        }

        // 校验密码是否正确
        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            log.warn("登录失败：密码错误，账号={}", identifier);
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        // 权限查询：根据角色加载用户权限
        Set<String> permissions = permissionService.getPermissionsByRole(user.getRole()).stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        log.info("用户权限加载完成，账号={}，权限数={}", identifier, permissions.size());

        // 生成 JWT Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), permissions);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        // 返回登录信息，包括 Token 和用户信息
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

    /**
     * 用户注册操作：验证用户名、邮箱、手机号唯一性，注册新用户
     *
     * @param registerDTO 用户注册的DTO
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String email = registerDTO.getEmail();
        String phone = registerDTO.getPhone();

        // 查询用户名、邮箱、手机号是否已存在
        Long count = userDao.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .or()
                .eq(SysUser::getPhone, phone)
                .or()
                .eq(SysUser::getEmail, email));

        if (count > 0) {
            log.warn("用户名或手机号或邮箱已存在，用户名：{}", username);
            throw new RegisterFailedException(MessageConstant.USERNAME_OR_PHONE_OR_EMAIL_EXISTS);
        }

        // 创建用户对象，设置密码并加密
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(PasswordUtil.encrypt(registerDTO.getPassword()));
        user.setRole("STUDENT");  // 默认角色为学生
        userDao.insert(user);  // 保存用户到数据库
    }

    /**
     * 获取个人信息
     *
     * @param userId 用户ID
     * @return 用户个人信息
     */
    @Override
    public SysUserDetailVO getPersonalInfo(Long userId) {
        log.info("获取个人信息，用户ID：{}", userId);
        SysUser user = userDao.selectById(userId);  // 查询用户
        if (user == null) {
            log.warn("用户不存在，用户ID：{}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        SysUserDetailVO userVO = new SysUserDetailVO();
        BeanUtils.copyProperties(user, userVO);  // 将用户信息拷贝到 VO 中
        return userVO;
    }

    /**
     * 更新个人信息
     *
     * @param userId 用户ID
     * @param updateUserDTO 更新的用户信息
     */
    @Override
    public void updatePersonalInfo(Long userId, UpdateUserDTO updateUserDTO) {
        log.info("更新个人信息，用户ID={}", userId);

        // 检查是否有有效字段需要更新
        if (updateUserDTO.getEmail() == null
                && updateUserDTO.getPhone() == null
                && updateUserDTO.getNickname() == null
                && updateUserDTO.getAvatar() == null) {
            log.info("更新个人信息跳过，无有效修改字段，用户ID={}", userId);
            return;
        }

        // 查询当前用户是否存在
        SysUser currentUser = userDao.selectById(userId);
        if (currentUser == null) {
            log.warn("更新个人信息失败，用户不存在，用户ID={}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验邮箱是否被其他用户占用
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().isBlank()) {
            Long emailCount = userDao.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getEmail, updateUserDTO.getEmail())
                            .ne(SysUser::getId, userId)
            );
            if (emailCount > 0) {
                log.warn("更新个人信息失败，邮箱已被占用，userId={}, email={}", userId, updateUserDTO.getEmail());
                throw new BaseException(400, MessageConstant.EMAIL_EXISTS);
            }
        }

        // 校验手机号是否被其他用户占用
        if (updateUserDTO.getPhone() != null && !updateUserDTO.getPhone().isBlank()) {
            Long phoneCount = userDao.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhone, updateUserDTO.getPhone())
                            .ne(SysUser::getId, userId)
            );
            if (phoneCount > 0) {
                log.warn("更新个人信息失败，手机号已被占用，userId={}, phone={}", userId, updateUserDTO.getPhone());
                throw new BaseException(400, MessageConstant.PHONE_EXISTS);
            }
        }

        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(userId);
        BeanUtils.copyProperties(updateUserDTO, user);  // 将更新的字段拷贝到用户对象

        userDao.updateById(user);  // 更新数据库
        log.info("个人信息更新成功，用户ID={}", userId);
    }

    /**
     * 修改密码操作
     *
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码的DTO
     */
    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        log.info("修改密码开始，userId={}", userId);

        // 查询用户是否存在
        SysUser user = userDao.selectById(userId);
        if (user == null) {
            log.warn("修改密码失败，用户不存在，userId={}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验旧密码是否正确
        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败，旧密码错误，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_ERROR);
        }

        // 校验新密码和旧密码是否相同
        if (PasswordUtil.matches(dto.getNewPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败，新密码与旧密码相同，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_SAME);
        }

        // 校验两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.warn("修改密码失败，两次密码不一致，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_CONFIRM_ERROR);
        }

        // 加密新密码
        String encrypted = PasswordUtil.encrypt(dto.getNewPassword());

        // 更新密码
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPasswordHash(encrypted);

        userDao.updateById(update);  // 更新数据库密码
        log.info("修改密码成功，userId={}", userId);
    }
}
