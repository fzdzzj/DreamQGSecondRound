package com.qg.server.controller.user;

import com.qg.common.enums.OperationTypeEnum;
import com.qg.common.result.Result;
import com.qg.pojo.entity.UserActionLog;
import com.qg.pojo.vo.UserActionLogVO;
import com.qg.server.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/log")
@Slf4j
@RequiredArgsConstructor
public class LogController {
    private final OperationLogService logService;
    @RequestMapping("/list")
    public Result<List<UserActionLogVO>> list() {
        // 1. 查询数据
        List<UserActionLog> logList = logService.list();

        // 2. 安全拷贝转VO
        List<UserActionLogVO> voList = logList.stream()
                .map(log -> {
                    UserActionLogVO vo = new UserActionLogVO();
                    vo.setId(log.getId());
                    vo.setUserId(log.getUserId());
                    vo.setActionType(log.getActionType());
                    vo.setActionTypeDesc(OperationTypeEnum.getDesc(log.getActionType()));
                    vo.setCreateTime(log.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        log.info("获取操作日志成功，总条数：{}", voList.size());
        return Result.success(voList);
    }
}
