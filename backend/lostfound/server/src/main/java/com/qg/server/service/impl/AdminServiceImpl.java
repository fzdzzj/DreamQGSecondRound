package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.*;
import com.qg.common.enums.UserRoleEnum;
import com.qg.common.enums.UserStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.AdminStatisticsQueryDTO;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.entity.UserActionLog;
import com.qg.pojo.vo.AdminStatisticsVO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.AdminService;
import com.qg.server.service.OperationLogService;
import com.qg.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务实现类
 * 提供管理员相关的业务逻辑实现
 * 包括用户管理、物品管理、操作日志管理等功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<UserDao, SysUser> implements AdminService {

    private final BizItemDao bizItemDao;
    private final UserDao userDao;
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户分页列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页结果
     * 1. 查询用户列表
     * 2. 转换为 VO 对象
     */
    @Override
    public PageResult<SysUserStatVO> userList(UserPageQueryDTO queryDTO) {
        log.info("管理员分页查询用户列表，queryDTO={}", queryDTO);
        if (queryDTO.getPageNum() == null) {
            queryDTO.setPageNum(DefaultPageConstant.DEFAULT_PAGE_NUM);
        }
        if (queryDTO.getPageSize() == null) {
            queryDTO.setPageSize(DefaultPageConstant.DEFAULT_PAGE_SIZE);
        }
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 1. 查询用户列表
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getId() != null, SysUser::getId, queryDTO.getId())
                .like(queryDTO.getUsername() != null, SysUser::getUsername, queryDTO.getUsername())
                .eq(queryDTO.getRole() != null, SysUser::getRole, queryDTO.getRole())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getStartTime() != null, SysUser::getLastLoginTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, SysUser::getLastLoginTime, queryDTO.getEndTime())
                .orderByDesc(SysUser::getLastLoginTime)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> resultPage = page(page, wrapper);
        log.info("分页查询用户列表结束,总条数：{}", resultPage.getTotal());
        // 2. 将查询结果转换为 VO 对象
        List<SysUserStatVO> list = resultPage.getRecords()
                .stream()
                .map(this::toSysUserStatVO)
                .toList();

        return new PageResult<>(list, resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    /**
     * 用户详情
     *
     * @param userId 用户ID
     * @return 用户详细信息
     * 1. 查询用户详情
     * 2. 转换为用户详情 VO 对象
     */
    @Override
    public SysUserDetailVO userDetail(Long userId) {
        log.info("管理员查看用户详情，userId={}", userId);
        // 1. 查询用户详情
        SysUser user = getById(userId);
        if (user == null) {
            log.warn("用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 2. 转换为用户详情 VO 对象
        log.info("用户详情查询成功，userId={}", userId);
        return toSysUserDetailVO(user);
    }

    /**
     * 封禁用户
     *
     * @param userId 用户ID
     *               1. 查询用户详情
     *               2. 检查用户状态
     *               3. 更新用户状态
     *               4. 禁用该用户的所有物品
     *               5. 返回成功响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        log.info("管理员封禁用户，userId={}", userId);
        // 1. 查询用户详情
        SysUser user = getById(userId);
        if (user == null) {
            log.warn("封禁用户失败，用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 2. 检查用户状态
        if (UserStatusConstant.DISABLE.equals(user.getStatus())) {
            log.info("用户当前已为禁用状态，无需重复封禁，userId={}", userId);
            return;
        }
        if (RoleConstant.ADMIN.equals(user.getRole()) || RoleConstant.SYSTEM.equals(user.getRole())) {
            log.warn("用户无权限操作此用户，userId={}", userId);
            throw new AbsentException(MessageConstant.USER_NOT_AUTHORIZED);
        }
        // 3. 更新用户状态
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(UserStatusConstant.DISABLE);

        updateById(updateUser);
        log.info("更新用户状态成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatusConstant.DISABLE);
        banUser(userId,"4");
        // 4. 关闭该用户所有物品
        List<BizItem> items = bizItemDao.selectList(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getUserId, userId));
        log.info("禁用该用户所有物品，userId={}", userId);
        items.forEach(item -> {
            item.setStatus(BizItemStatusConstant.CLOSED);
            bizItemDao.updateById(item);
        });
        // 5. 返回成功响应
        log.info("管理员封禁用户成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatusConstant.DISABLE);
    }


    /**
     * 解封用户
     *
     * @param userId 用户ID
     *               1. 查询用户详情
     *               2. 检查用户状态
     *               3. 更新用户状态
     *               4. 删除用户封禁缓存
     *               5. 返回成功响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long userId) {
        log.info("管理员解封用户，userId={}", userId);
        // 1. 查询用户详情
        SysUser user = getById(userId);
        if (user == null) {
            log.warn("解封用户失败，用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 2. 检查用户状态
        if (UserStatusConstant.ENABLE.equals(user.getStatus())) {
            log.info("用户当前已为启用状态，无需重复解封，userId={}", userId);
            return;
        }
        if (RoleConstant.ADMIN.equals(user.getRole()) || RoleConstant.SYSTEM.equals(user.getRole())) {
            log.warn("用户无权限操作此用户，userId={}", userId);
            throw new AbsentException(MessageConstant.USER_NOT_AUTHORIZED);
        }
        // 3. 更新用户状态
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(UserStatusConstant.ENABLE);
        updateById(updateUser);
        log.info("更新用户状态成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatusConstant.ENABLE);
        //4. 删除用户封禁缓存
        redisTemplate.delete(RedisConstant.USER_BANNED_KEY + userId);
        // 5. 返回成功响应
        log.info("管理员解封用户成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatusConstant.ENABLE);
    }

    /**
     * 删除物品（逻辑删除）
     *
     * @param itemId 物品ID
     *               1. 查询物品详情
     *               2. 检查物品状态
     *               3. 更新物品状态
     *               4. 清理物品缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        // 1. 查询物品详情
        BizItem bizItem = bizItemDao.selectById(itemId);
        if (bizItem == null) {
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        //2.删除物品
        bizItemDao.deleteById(itemId);
        log.info("删除物品成功，itemId={}", itemId);
        //3. 更新物品状态
        BizItem updateItem = new BizItem();
        updateItem.setId(itemId);
        updateItem.setStatus(BizItemStatusConstant.DELETED);
        bizItemDao.updateById(updateItem);
        log.info("更新物品状态成功，itemId={}, oldStatus={}, newStatus={}",
                itemId, bizItem.getStatus(), BizItemStatusConstant.DELETED);
        //4. 清理物品缓存
        evictItemCaches(itemId); // 清理缓存
    }

    /**
     * 平台统计
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     * 1. 获取查询条件
     * 2. 检查时间范围是否有效
     * 3. 统计发布信息数量
     * 4. 统计找回物品数量
     * 5. 统计活跃用户数
     * 6. 统计用户数量
     */
    @Override
    public AdminStatisticsVO statistics(AdminStatisticsQueryDTO queryDTO) {
        log.info("管理员获取平台统计开始，queryDTO={}", queryDTO);
        // 1. 获取查询条件
        LocalDateTime startTime = queryDTO == null ? null : queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO == null ? null : queryDTO.getEndTime();
        // 2. 检查时间范围是否有效
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能大于结束时间");
        }
        log.info("开始时间：{}，结束时间：{}", startTime, endTime);

        // 默认时间范围：最近7天
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now();
            startTime = endTime.minusDays(7);
            log.info("未传时间范围，使用默认时间范围：startTime={}, endTime={}", startTime, endTime);
        }

        AdminStatisticsVO vo = new AdminStatisticsVO();

        // 3. 统计发布信息数量
        Long publishCount = bizItemDao.selectCount(
                new LambdaQueryWrapper<BizItem>()
                        .ge(startTime != null, BizItem::getCreateTime, startTime)
                        .le(endTime != null, BizItem::getCreateTime, endTime)
        );
        log.info("发布信息数量：{}", publishCount);

        // 4. 统计找回物品数量
        Long foundCount = bizItemDao.selectCount(
                new LambdaQueryWrapper<BizItem>()
                        .eq(BizItem::getStatus, BizItemStatusConstant.MATCHED)
                        .ge(startTime != null, BizItem::getCreateTime, startTime)
                        .le(endTime != null, BizItem::getCreateTime, endTime)
        );
        log.info("找回物品数量：{}", foundCount);

        // 5. 统计活跃用户数
        Long activeUserCount = userDao.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .ge(startTime != null, SysUser::getLastLoginTime, startTime)
                        .le(endTime != null, SysUser::getLastLoginTime, endTime)
        );
        log.info("活跃用户数：{}", activeUserCount);
        // 6. 统计结果
        vo.setPublishCount(publishCount == null ? 0L : publishCount);
        vo.setFoundCount(foundCount == null ? 0L : foundCount);
        vo.setActiveUserCount(activeUserCount == null ? 0L : activeUserCount);

        log.info("平台统计完成，publishCount={}, foundCount={}, activeUserCount={}",
                vo.getPublishCount(), vo.getFoundCount(), vo.getActiveUserCount());

        return vo;
    }

    /**
     * 获取用户详情
     *
     * @param user 用户实体
     * @return 用户列表VO
     */
    private SysUserStatVO toSysUserStatVO(SysUser user) {
        SysUserStatVO vo = new SysUserStatVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleDesc(UserRoleEnum.getDescByCode(user.getRole()));
        vo.setStatusDesc(UserStatusEnum.getDescByCode(user.getStatus()));
        return vo;
    }

    /**
     * 获取用户详情
     *
     * @param user 用户实体
     * @return 用户详情VO
     */
    private SysUserDetailVO toSysUserDetailVO(SysUser user) {
        SysUserDetailVO vo = new SysUserDetailVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleDesc(UserRoleEnum.getDescByCode(user.getRole()));
        vo.setStatusDesc(UserStatusEnum.getDescByCode(user.getStatus()));
        return vo;
    }

    /**
     * 清理物品缓存
     *
     * @param itemId 物品ID
     */

    private void evictItemCaches(Long itemId) {
        // 清理物品缓存
        redisTemplate.delete("item:detail:" + itemId);
    }

    /**
     * 封禁用户
     *
     * @param userId     用户ID
     * @param actionType 封禁原因
     *                   1. 封禁用户
     *                   2. 记录封禁日志
     */
    public void banUser(Long userId, String actionType) {
        // 1. 封禁用户
        redisTemplate.opsForValue().set(RedisConstant.USER_BANNED_KEY + userId, true);
        redisTemplate.expire(RedisConstant.USER_BANNED_KEY + userId, Duration.ofDays(8));
        log.info("用户 {} 被封禁，封禁原因：{}", userId, actionType);
        SysUser sysUser = userService.getById(userId);
        sysUser.setStatus(0);
        userService.updateById(sysUser);
        log.info("用户 {} 封禁成功", userId);
        // 2. 记录封禁日志
        log.info("用户 {} 封禁成功", userId);
        UserActionLog userActionLog = new UserActionLog();
        userActionLog.setUserId(userId);
        userActionLog.setActionType(actionType);
        operationLogService.save(userActionLog);
        log.info("用户 {} 封禁日志记录成功", userId);
    }
}
