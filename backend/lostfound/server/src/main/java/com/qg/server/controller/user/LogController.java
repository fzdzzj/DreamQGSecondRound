package com.qg.server.controller.user;

import com.qg.common.enums.OperationTypeEnum;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.PageLogDTO;
import com.qg.pojo.entity.UserActionLog;
import com.qg.pojo.vo.UserActionLogVO;
import com.qg.server.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志接口
 * 提供操作日志相关的接口，如获取操作日志、分页获取操作日志等
 */
@RestController
@RequestMapping("/log")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "操作日志接口", description = "操作日志接口")
public class LogController {
    private final OperationLogService logService;

    /**
     * 获取操作日志
     *
     * @return 操作日志列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取操作日志")
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

    /**
     * 分页获取操作日志
     *
     * @param dto 分页查询DTO
     * @return 分页查询结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页获取操作日志")
    public Result<PageResult<UserActionLogVO>> page(@RequestBody PageLogDTO dto) {
        log.info("分页获取操作日志开始，参数：{}", dto);
        PageResult<UserActionLogVO> logList = logService.pageQuery(dto);
        log.info("分页获取操作日志成功，总条数：{}", logList.getTotal());
        return Result.success(logList);
    }
}
