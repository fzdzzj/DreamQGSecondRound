package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;

public interface AdminService {
    /**
     * 用户分页列表
     */
    PageResult<SysUserStatVO> userList(UserPageQueryDTO queryDTO);

    /**
     * 用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    SysUserDetailVO userDetail(Long userId);
}
