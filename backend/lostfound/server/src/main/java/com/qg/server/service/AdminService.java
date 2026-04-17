package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.AdminAiStatisticsQueryDTO;
import com.qg.pojo.dto.AdminStatisticsQueryDTO;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.AdminAiStatisticsVO;
import com.qg.pojo.vo.AdminStatisticsVO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;

import java.util.List;

/**
 * 管理员服务
 */
public interface AdminService extends IService<SysUser> {

    /**
     * 用户分页列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    PageResult<SysUserStatVO> userList(UserPageQueryDTO queryDTO);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    SysUserDetailVO userDetail(Long userId);

    /**
     * 封禁用户
     *
     * @param userId 用户ID
     */
    void disableUser(Long userId);

    /**
     * 解封用户
     *
     * @param userId 用户ID
     */
    void enableUser(Long userId);

    /**
     * 管理员删除物品
     *
     * @param itemId 物品ID
     */
    void deleteItem(Long itemId);

    /**
     * 平台统计
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     */
    AdminStatisticsVO statistics(AdminStatisticsQueryDTO queryDTO);
    /**
     * AI统计报告
     *
     */
    PageResult<AdminAiStatisticsVO> aiStatistics(AdminAiStatisticsQueryDTO dto);
}
