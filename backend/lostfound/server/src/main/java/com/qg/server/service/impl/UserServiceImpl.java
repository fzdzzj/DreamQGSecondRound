package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.*;
import com.qg.common.exception.*;
import com.qg.common.util.JwtUtil;
import com.qg.common.util.PasswordUtil;
import com.qg.pojo.dto.*;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.LoginResponseVO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.UserLoginVO;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.EmailVerificationCodeService;
import com.qg.server.service.PermissionService;
import com.qg.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * <p>
 * 核心职责：
 * 1. 用户登录操作：验证用户身份，生成访问 Token 和刷新 Token
 * 2. 用户注册操作：创建新用户
 * 3. 用户信息查询操作：根据用户 ID 查询用户详情
 * 4. 用户权限查询操作：根据用户 ID 查询用户权限
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserDao, SysUser> implements UserService {

    private final UserDao userDao;  // 用户数据访问层
    private final PermissionService permissionService;  // 权限服务层
    private final EmailVerificationCodeService emailVerificationCodeService;  // 邮箱验证码服务层
    private final JwtUtil jwtUtil;  // JWT 工具类

    /**
     * 用户登录操作：验证用户身份，生成访问 Token 和刷新 Token
     *
     * @param loginDTO 登录请求的DTO
     * @return 登录后的 token 和用户信息
     * @throws AccountNotFoundException 用户不存在或已删除
     * @throws LoginFailedException     登录失败
     * @throws BaseException            其他异常
     *                                  判断登录类型是否为密码登录
     *                                  1. 校验登录类型是否为密码登录
     *                                  2. 校验用户标识（手机号或邮箱）是否存在
     *                                  3. 校验用户状态（账号是否禁用）
     *                                  4. 校验密码是否正确
     *                                  验证码登录
     *                                  1. 校验验证码是否正确
     *                                  2. 查询用户
     *                                  3. 校验用户是否存在
     *                                  4. 校验用户状态（账号是否禁用）
     *                                  <p>
     *                                  <p>
     *                                  更新最后登录信息，登录成功，生成访问 Token 和刷新 Token，封装响应数据
     */
    @Override
    public LoginResponseVO login(LoginDTO loginDTO, HttpServletRequest request) {
        SysUser user;
        // 校验登录类型是否为密码登录
        if (loginDTO.getLoginType().equals(LoginTypeConstant.PASSWORD)) {
            String identifier = loginDTO.getIdentifier();  // 用户标识（邮箱或手机）
            String password = loginDTO.getPassword();  // 用户密码
            log.info("用户发起登录请求，账号={}", identifier);

            // 1.根据账号类型（手机号或邮箱）查询用户
            if (identifier.contains("@")) {
                user = userDao.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, identifier)
                        .eq(SysUser::getDeleted, 0));  // 未删除的用户
            } else {
                // 2.根据手机号查询用户
                user = userDao.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, identifier)
                        .eq(SysUser::getDeleted, 0));  // 未删除的用户
            }

            // 3.用户不存在或已删除，抛出异常
            if (user == null) {
                log.warn("登录失败：用户不存在或已删除，账号={}", identifier);
                throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
            }

            // 4.校验账号状态（账号是否禁用）
            if (user.getStatus() == null || !UserStatusConstant.ENABLE.equals(user.getStatus())) {
                log.warn("登录失败：用户已被禁用，账号={}", identifier);
                throw new AccountLockedException(MessageConstant.ACCOUNT_DISABLED);
            }

            // 5.校验密码是否正确
            if (!PasswordUtil.matches(password, user.getPasswordHash())) {
                log.warn("登录失败：密码错误，账号={}", identifier);
                throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
            }
        } else {
            // 登录类型为邮箱登录
            String email = loginDTO.getIdentifier();
            String code = loginDTO.getCode();

            // 1.校验验证码（LOGIN 类型）
            boolean ok = emailVerificationCodeService.verifyCode(email, CodeTypeConstant.LOGIN, code);
            if (!ok) {
                log.warn("登录失败：验证码错误，邮箱={}", email);
                throw new BaseException(401, MessageConstant.CODE_ERROR);
            }
            // 2.根据邮箱查询用户
            user = userDao.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email)
                    .eq(SysUser::getDeleted, 0));
            // 3.用户不存在或已删除，抛出异常
            if (user == null) {
                log.warn("登录失败：用户不存在或已删除，邮箱={}", email);
                throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
            }
            // 4.校验用户状态（账号是否禁用）
            if (user.getStatus() == null || !UserStatusConstant.ENABLE.equals(user.getStatus())) {
                log.warn("登录失败：用户已被禁用，邮箱={}", email);
                throw new AccountLockedException(MessageConstant.ACCOUNT_DISABLED);
            }

        }
        // 更新最后登录信息
        String clientIp = getClientIp(request);
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setLastLoginIp(clientIp);
        updateUser.setLastLoginTime(LocalDateTime.now());
        userDao.updateById(updateUser);

        Set<String> permissions = permissionService.getPermissionsByRole(user.getRole())
                .stream()
                .map(String::trim)
                .collect(Collectors.toSet());

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
        // 封装返回数据
        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setAccessToken(accessToken);
        responseVO.setRefreshToken(refreshToken);

        UserLoginVO userInfo = new UserLoginVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(user.getRole());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());

        responseVO.setUser(userInfo);

        log.info("用户登录成功，userId={}, ip={}", user.getId(), clientIp);
        return responseVO;
    }

    /**
     * 用户注册操作：验证用户名、邮箱、手机号唯一性，注册新用户
     *
     * @param registerDTO 用户注册的DTO
     *                    1. 校验邮箱是否已注册
     *                    2. 校验注册验证码（REGISTER 类型）
     *                    3. 校验用户名、手机号、邮箱是否已存在
     *                    4. 创建用户对象，设置密码并加密
     *                    5. 保存用户到数据库
     *
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        String code = registerDTO.getCode();

        log.info("用户注册，邮箱={}", email);
        // 1. 邮箱是否已注册
        SysUser exist = userDao.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email)
                .eq(SysUser::getDeleted, 0));
        if (exist != null) {
            log.warn("用户注册失败：邮箱已存在，邮箱={}", email);
            throw new RegisterFailedException(MessageConstant.EMAIL_EXISTS);
        }

        // 2. 校验注册验证码（REGISTER 类型）
        boolean ok = emailVerificationCodeService.verifyCode(email, CodeTypeConstant.REGISTER, code);
        if (!ok) {
            log.warn("用户注册失败：验证码错误，邮箱={}", email);
            throw new BaseException(401, MessageConstant.CODE_ERROR);
        }


        log.info("用户发起注册请求，用户名：{}", registerDTO.getUsername());
        String username = registerDTO.getUsername();
        String phone = registerDTO.getPhone();

        // 3. 校验用户名、手机号、邮箱是否已存在
        Long count = userDao.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .or()
                .eq(SysUser::getPhone, phone)
                .or()
                .eq(SysUser::getEmail, email));
        log.info("查询到的用户数量：{}", count);
        if (count > 0) {
            log.warn("用户注册失败：用户名或手机号或邮箱已存在，用户名或手机号或邮箱：{}", username, phone, email);
            throw new RegisterFailedException(MessageConstant.USERNAME_OR_PHONE_OR_EMAIL_EXISTS);
        }

        // 5.创建用户对象，设置密码并加密
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(PasswordUtil.encrypt(registerDTO.getPassword()));
        user.setRole(RoleConstant.USER);  // 默认角色为1：普通用户
        user.setStatus(UserStatusConstant.ENABLE);
        user.setNickname(registerDTO.getNickname());
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
     * @param userId        用户ID
     * @param updateUserDTO 更新的用户信息
     *                      <p>
     *                      1. 检查是否有有效字段需要更新
     *                      2. 查询当前用户是否存在
     *                      3. 校验邮箱是否被其他用户占用
     *                      4. 校验手机号是否被其他用户占用
     *                      5. 更新用户信息
     */
    @Override
    public void updatePersonalInfo(Long userId, UpdateUserDTO updateUserDTO) {
        log.info("更新个人信息，用户ID={}", userId);

        // 1.检查是否有有效字段需要更新
        if (updateUserDTO.getEmail() == null
                && updateUserDTO.getPhone() == null
                && updateUserDTO.getNickname() == null
                && updateUserDTO.getAvatar() == null) {
            log.info("更新个人信息跳过，无有效修改字段，用户ID={}", userId);
            return;
        }

        // 2. 查询当前用户是否存在
        SysUser currentUser = userDao.selectById(userId);
        if (currentUser == null) {
            log.warn("更新个人信息失败，用户不存在，用户ID={}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 3. 校验邮箱是否被其他用户占用
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

        // 4. 校验手机号是否被其他用户占用
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

        // 5. 更新用户信息
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
     * @param dto    修改密码的DTO
     *               <p>
     *               1. 查询用户是否存在
     *               2. 校验旧密码是否正确
     *               3. 校验新密码和旧密码是否相同
     *               4. 校验两次密码是否一致
     *               5. 加密新密码
     *               6. 更新密码
     */
    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        log.info("修改密码开始，userId={}", userId);

        // 1. 查询用户是否存在
        SysUser user = userDao.selectById(userId);
        if (user == null) {
            log.warn("修改密码失败，用户不存在，userId={}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验旧密码是否正确
        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败，旧密码错误，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_ERROR);
        }

        // 3.校验新密码和旧密码是否相同
        if (PasswordUtil.matches(dto.getNewPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败，新密码与旧密码相同，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_SAME);
        }

        // 4. 校验两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.warn("修改密码失败，两次密码不一致，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_CONFIRM_ERROR);
        }

        // 5. 加密新密码
        String encrypted = PasswordUtil.encrypt(dto.getNewPassword());

        // 6. 更新密码
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPasswordHash(encrypted);

        userDao.updateById(update);  // 更新数据库密码
        log.info("修改密码成功，userId={}", userId);
    }

    /**
     * 通过验证码修改密码
     *
     * @param userId 用户ID
     * @param dto    修改密码的DTO
     *               <p>
     *               1. 校验验证码（类型：RESET_PASSWORD）
     *               2. 查询当前用户（确保只能改自己的邮箱）
     *               3. 安全校验：必须是当前登录用户的邮箱，防止越权
     *               4. 校验两次密码是否一致
     *               5. 加密新密码
     *               6. 更新密码
     */
    @Override
    public void changePasswordByCode(Long userId, ChangePasswordByCodeDTO dto) {
        // 1. 校验验证码（类型：RESET_PASSWORD）
        boolean verifySuccess = emailVerificationCodeService.verifyCode(
                dto.getEmail(),
                CodeTypeConstant.RESET_PASSWORD,
                dto.getCode()
        );
        log.info("校验验证码结果，userId={}, code={}, verifySuccess={}", userId, dto.getCode(), verifySuccess);

        if (!verifySuccess) {
            log.warn("修改密码失败，验证码错误，userId={}", userId);
            throw new BaseException(400, MessageConstant.CODE_ERROR);
        }

        // 2. 查询当前用户（确保只能改自己的邮箱）
        SysUser user = getById(userId);
        if (user == null) {
            log.warn("修改密码失败，用户不存在，userId={}", userId);
            throw new RuntimeException("用户不存在");
        }

        // 3. 安全校验：必须是当前登录用户的邮箱，防止越权
        if (!user.getEmail().equals(dto.getEmail())) {
            log.warn("修改密码失败，邮箱与当前用户不匹配，userId={}", userId);
            throw new BaseException(400, MessageConstant.EMAIL_MISMATCH);
        }
        // 4. 校验两次密码是否一致
        if (PasswordUtil.matches(dto.getNewPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败，新密码与旧密码相同，userId={}", userId);
            throw new BaseException(400, MessageConstant.PASSWORD_SAME);
        }
        log.info("修改密码开始，userId={}", userId);
        // 5. 加密新密码
        String newPassword = PasswordUtil.encrypt(dto.getNewPassword());

        // 6. 更新密码
        user.setPasswordHash(newPassword);
        updateById(user);
        log.info("修改密码成功，userId={}", userId);
    }


    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        return request.getRemoteAddr();
    }
}
