package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.enums.UserRoleEnum;
import com.qg.common.enums.UserStatusEnum;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.SysUserStatVO;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
    private SysUserStatVO toSysUserStatVO(SysUser user) {
        SysUserStatVO vo = new SysUserStatVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleDesc(UserRoleEnum.getDescByCode(user.getRole()));
        vo.setStatusDesc(UserStatusEnum.getDescByCode(user.getStatus()));
        return vo;
    }

}
