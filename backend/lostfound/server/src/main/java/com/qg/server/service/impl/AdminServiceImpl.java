package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.UserStatus;
import com.qg.common.enums.UserRoleEnum;
import com.qg.common.enums.UserStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.AdminStatisticsQueryDTO;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.AdminStatisticsVO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserDao userDao;
    private final BizItemDao bizItemDao;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 详情缓存 key 前缀
     * 示例：item:detail:1001
     */
    private static final String ITEM_DETAIL_KEY = "item:detail:";

    /**
     * 分页缓存 key 前缀
     * 示例：item:page:type=LOST:keyword=校园卡:location=图书馆:page=1:size=10
     */
    private static final String ITEM_PAGE_KEY = "item:page:";

    @Override
    public PageResult<SysUserStatVO> userList(UserPageQueryDTO queryDTO) {
        log.info("管理员分页查询用户列表，queryDTO={}", queryDTO);

        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getId() != null, SysUser::getId, queryDTO.getId())
                .like(StringUtils.isNotBlank(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
                .eq(StringUtils.isNotBlank(queryDTO.getRole()), SysUser::getRole, queryDTO.getRole())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getStartTime() != null, SysUser::getLastLoginTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, SysUser::getLastLoginTime, queryDTO.getEndTime())
                .orderByDesc(SysUser::getLastLoginTime)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> resultPage = userDao.selectPage(page, wrapper);

        List<SysUserStatVO> list = resultPage.getRecords()
                .stream()
                .map(this::toSysUserStatVO)
                .toList();

        return new PageResult<>(
                list,
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize()
        );
    }

    @Override
    public SysUserDetailVO userDetail(Long userId) {
        log.info("管理员查看用户详情，userId={}", userId);

        SysUser user = userDao.selectById(userId);
        if (user == null) {
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return toSysUserDetailVO(user);
    }

    /**
     * 管理员封禁用户
     *
     * 说明：
     * 1. 仅对未被逻辑删除且存在的用户生效
     * 2. 若用户不存在，则抛出不存在异常
     * 3. 若用户当前已是禁用状态，则直接返回，保证接口幂等
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        log.info("管理员封禁用户，userId={}", userId);

        SysUser user = userDao.selectById(userId);
        if (user == null) {
            log.warn("封禁用户失败，用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (UserStatus.DISABLE.equals(user.getStatus())) {
            log.info("用户当前已为禁用状态，无需重复封禁，userId={}", userId);
            return;
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(UserStatus.DISABLE);

        userDao.updateById(updateUser);

        log.info("管理员封禁用户成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatus.DISABLE);
    }
    /**
     * 管理解封用户
     *
     * 说明：
     * 1. 仅对未被逻辑删除且存在的用户生效
     * 2. 若用户不存在，则抛出不存在异常
     * 3. 若用户当前已是启用状态，则直接返回，保证接口幂等
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long userId) {
        log.info("管理员解封用户，userId={}", userId);

        SysUser user = userDao.selectById(userId);
        if (user == null) {
            log.warn("解封用户失败，用户不存在或已被逻辑删除，userId={}", userId);
            throw new AbsentException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (UserStatus.ENABLE.equals(user.getStatus())) {
            log.info("用户当前已为启用状态，无需重复解封，userId={}", userId);
            return;
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(UserStatus.ENABLE);

        userDao.updateById(updateUser);

        log.info("管理员解封用户成功，userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), UserStatus.ENABLE);
    }
    /**
     * 管理员删除物品（逻辑删除）
     *
     * 说明：
     * 1. 仅对存在的物品生效
     * 2. 若物品不存在（或已逻辑删除），抛异常
     * 3. 若已是删除状态，则直接返回（幂等）
     *
     * @param itemId 物品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        BizItem bizItem = bizItemDao.selectById(itemId);
        if (bizItem == null) {
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        bizItemDao.deleteById(itemId);
        evictItemCaches(itemId);
    }

    /**
     * 管理员平台统计
     *
     * 统计内容：
     * 1. 发布信息数量（指定时间段内创建，且未被逻辑删除）
     * 2. 找回物品数量（指定时间段内创建，且状态为 MATCHED）
     * 3. 活跃用户数（指定时间段内登录）
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
                        .eq(BizItem::getStatus, BizItemStatus.MATCHED)
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
        redisTemplate.delete(ITEM_DETAIL_KEY + itemId);

        Set<String> keys = redisTemplate.keys(ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }


}
