package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PageLogDTO;
import com.qg.pojo.entity.UserActionLog;
import com.qg.pojo.vo.UserActionLogVO;

/**
 * 用户操作日志服务接口
 */
public interface OperationLogService extends IService<UserActionLog> {
    /**
     * 分页查询用户操作日志
     * @param dto 分页查询参数
     * @return 分页查询结果
     */
    PageResult<UserActionLogVO> pageQuery(PageLogDTO dto);
}
