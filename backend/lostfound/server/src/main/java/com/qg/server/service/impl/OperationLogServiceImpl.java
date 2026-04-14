package com.qg.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.pojo.entity.UserActionLog;
import com.qg.server.mapper.UserActionLogDao;
import com.qg.server.service.OperationLogService;

public class OperationLogServiceImpl extends ServiceImpl<UserActionLogDao, UserActionLog> implements OperationLogService {
}
