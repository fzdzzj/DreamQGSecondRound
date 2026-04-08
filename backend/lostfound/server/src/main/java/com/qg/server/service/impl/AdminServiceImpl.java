package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.UserStatus;
import com.qg.common.enums.UserRoleEnum;
import com.qg.common.enums.UserStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserDao userDao;

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


}
