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

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<UserDao, SysUser> implements AdminService {

    private final BizItemDao bizItemDao;  // 物品数据访问层
    private final UserDao userDao;
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 用户分页列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页结果
     */
    @Override
    public PageResult<SysUserStatVO> userList(UserPageQueryDTO queryDTO) {
        log.info("管理员分页查询用户列表，queryDTO={}", queryDTO);

        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getId() != null, SysUser::getId, queryDTO.getId())
                .like(queryDTO.getUsername() != null, SysUser::getUsername, queryDTO.getUsername())
                .eq(queryDTO.getRole() != null, SysUser::getRole, queryDTO.getRole())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getStartTime() != null, SysUser::getLastLoginTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, SysUser::getLastLoginTime, queryDTO.getEndTime())
                .orderByDesc(SysUser::getLastLoginTime)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> resultPage = baseMapper.selectPage(page, wrapper); // 使用 MyBatis-Plus 提供的 selectPage

        // 将查询结果转换为 VO 对象
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
     */
    @Override
    public SysUserDetailVO userDetail(Long userId) {
        log.info("管理员查看用户详情，userId={}", userId);

        SysUser user = baseMapper.selectById(userId);  // 使用 IService 提供的 getById 方法
        if (user == null) {
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return toSysUserDetailVO(user);
    }

    /**
     * 封禁用户
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        log.info("管理员封禁用户，userId={}", userId);

        SysUser user = baseMapper.selectById(userId);  // 使用 IService 提供的 getById 方法
        if (user == null) {
            log.warn("封禁用户失败，用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (UserStatusConstant.DISABLE.equals(user.getStatus())) {
            log.info("用户当前已为禁用状态，无需重复封禁，userId={}", userId);
            return;
        }
        if (RoleConstant.ADMIN.equals(user.getRole())|| RoleConstant.SYSTEM.equals(user.getRole())) {
            log.warn("用户无权限操作此用户，userId={}", userId);
            throw new AbsentException(MessageConstant.USER_NOT_AUTHORIZED);
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(UserStatusConstant.DISABLE);

        baseMapper.updateById(updateUser);  // 使用 IService 提供的 updateById 方法
        banUser(userId,null);
        //查找该用户的所有物品
        List<BizItem> items = bizItemDao.selectList(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getUserId, userId));
        //禁用该用户的所有物品
        items.forEach(item -> {
            item.setStatus(BizItemStatusConstant.CLOSED);
            bizItemDao.updateById(item);
        });

        log.info("管理员封禁用户成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatusConstant.DISABLE);
    }



    /**
     * 解封用户
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long userId) {
        log.info("管理员解封用户，userId={}", userId);

        SysUser user = baseMapper.selectById(userId);  // 使用 IService 提供的 getById 方法
        if (user == null) {
            log.warn("解封用户失败，用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (UserStatusConstant.ENABLE.equals(user.getStatus())) {
            log.info("用户当前已为启用状态，无需重复解封，userId={}", userId);
            return;
        }
        if (RoleConstant.ADMIN.equals(user.getRole())|| RoleConstant.SYSTEM.equals(user.getRole())) {
            log.warn("用户无权限操作此用户，userId={}", userId);
            throw new AbsentException(MessageConstant.USER_NOT_AUTHORIZED);
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(UserStatusConstant.ENABLE);

        baseMapper.updateById(updateUser);  // 使用 IService 提供的 updateById 方法
        redisTemplate.delete(RedisConstant.USER_BANNED_KEY + userId);

        log.info("管理员解封用户成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatusConstant.ENABLE);
    }

    /**
     * 删除物品（逻辑删除）
     *
     * @param itemId 物品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        BizItem bizItem = bizItemDao.selectById(itemId);  // 使用 getById 方法
        if (bizItem == null) {
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        bizItemDao.deleteById(itemId);  // 使用 deleteById 方法
        evictItemCaches(itemId);  // 清理缓存
    }

    /**
     * 平台统计
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     */
    @Override
    public AdminStatisticsVO statistics(AdminStatisticsQueryDTO queryDTO) {
        log.info("管理员获取平台统计开始，queryDTO={}", queryDTO);

        LocalDateTime startTime = queryDTO == null ? null : queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO == null ? null : queryDTO.getEndTime();
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能大于结束时间");
        }

        // 默认时间范围：最近7天
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now();
            startTime = endTime.minusDays(7);
            log.info("未传时间范围，使用默认时间范围：startTime={}, endTime={}", startTime, endTime);
        }

        AdminStatisticsVO vo = new AdminStatisticsVO();

        // 发布信息数量
        Long publishCount = bizItemDao.selectCount(
                new LambdaQueryWrapper<BizItem>()
                        .ge(startTime != null, BizItem::getCreateTime, startTime)
                        .le(endTime != null, BizItem::getCreateTime, endTime)
        );

        // 找回物品数量
        Long foundCount = bizItemDao.selectCount(
                new LambdaQueryWrapper<BizItem>()
                        .eq(BizItem::getStatus, BizItemStatusConstant.MATCHED)
                        .ge(startTime != null, BizItem::getCreateTime, startTime)
                        .le(endTime != null, BizItem::getCreateTime, endTime)
        );

        // 活跃用户数
        Long activeUserCount = userDao.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .ge(startTime != null, SysUser::getLastLoginTime, startTime)
                        .le(endTime != null, SysUser::getLastLoginTime, endTime)
        );

        vo.setPublishCount(publishCount == null ? 0L : publishCount);
        vo.setFoundCount(foundCount == null ? 0L : foundCount);
        vo.setActiveUserCount(activeUserCount == null ? 0L : activeUserCount);

        log.info("平台统计完成，publishCount={}, foundCount={}, activeUserCount={}",
                vo.getPublishCount(), vo.getFoundCount(), vo.getActiveUserCount());

        return vo;
    }

    private SysUserStatVO toSysUserStatVO(SysUser user) {
        SysUserStatVO vo = new SysUserStatVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleDesc(UserRoleEnum.getDescByCode(user.getRole()));
        vo.setStatusDesc(UserStatusEnum.getDescByCode(user.getStatus()));
        return vo;
    }

    private SysUserDetailVO toSysUserDetailVO(SysUser user) {
        SysUserDetailVO vo = new SysUserDetailVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleDesc(UserRoleEnum.getDescByCode(user.getRole()));
        vo.setStatusDesc(UserStatusEnum.getDescByCode(user.getStatus()));
        return vo;
    }

    private void evictItemCaches(Long itemId) {
        // 清理物品缓存
        redisTemplate.delete("item:detail:" + itemId);
    }
    public void banUser(Long userId,String actionType) {
        redisTemplate.opsForValue().set(RedisConstant.USER_BANNED_KEY + userId, true);
        redisTemplate.expire(RedisConstant.USER_BANNED_KEY + userId, Duration.ofDays(8));
        log.info("用户 {} 被封禁，封禁原因：{}", userId,actionType);
        SysUser sysUser = userService.getById(userId);
        sysUser.setStatus(0);
        userService.updateById(sysUser);
        log.info("用户 {} 封禁成功", userId);
        UserActionLog userActionLog = new UserActionLog();
        userActionLog.setUserId(userId);
        userActionLog.setActionType(actionType);
        operationLogService.save(userActionLog);
    }
}
