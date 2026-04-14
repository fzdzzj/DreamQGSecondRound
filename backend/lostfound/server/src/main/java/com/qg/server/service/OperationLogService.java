package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PageLogDTO;
import com.qg.pojo.entity.UserActionLog;
import com.qg.pojo.vo.UserActionLogVO;

public interface OperationLogService extends IService<UserActionLog> {
    PageResult<UserActionLogVO> pageQuery(PageLogDTO dto);
}
