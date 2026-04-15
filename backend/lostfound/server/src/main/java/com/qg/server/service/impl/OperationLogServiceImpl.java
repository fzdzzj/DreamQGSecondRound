package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.enums.OperationTypeEnum;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PageLogDTO;
import com.qg.pojo.entity.UserActionLog;
import com.qg.pojo.vo.UserActionLogVO;
import com.qg.server.mapper.UserActionLogDao;
import com.qg.server.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 * 提供操作日志相关的业务逻辑实现
 */
@Service
@Slf4j
public class OperationLogServiceImpl extends ServiceImpl<UserActionLogDao, UserActionLog> implements OperationLogService {
    /**
     * 分页查询操作日志
     *
     * @param dto 分页参数
     * @return 分页结果
     * 1. 处理默认安全时间范围（可选）
     * 2. 添加查询条件
     * 3. 执行分页查询
     */
    @Override
    public PageResult<UserActionLogVO> pageQuery(PageLogDTO dto) {
        log.info("分页查询操作日志开始");
        Page<UserActionLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<UserActionLog> wrapper = new LambdaQueryWrapper<>();
        // 1. 处理默认安全时间范围（可选）
        if (dto.getEndTime() == null) {
            dto.setEndTime(LocalDateTime.now());
        }
        if (dto.getStartTime() == null) {
            dto.setStartTime(dto.getEndTime().minusDays(7)); // 固定查最近7天
        }
        // 2. 添加查询条件
        wrapper.eq(dto.getUserId() != null, UserActionLog::getUserId, dto.getUserId())
                .eq(dto.getOperationType() != null, UserActionLog::getActionType, dto.getOperationType())
                .ge(dto.getStartTime() != null, UserActionLog::getCreateTime, dto.getStartTime())
                .le(dto.getEndTime() != null, UserActionLog::getCreateTime, dto.getEndTime())
                .orderByDesc(UserActionLog::getCreateTime);
        // 3. 执行分页查询
        page = this.page(page, wrapper);
        log.info("分页查询操作日志结束,总条数：{}", page.getTotal());
        return convert(page);
    }

    /**
     * 转换为VO
     *
     * @param page 分页结果
     * @return VO结果
     */
    private PageResult<UserActionLogVO> convert(Page<UserActionLog> page) {
        PageResult<UserActionLogVO> result = new PageResult<UserActionLogVO>();
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setList(page.getRecords().stream().map(item -> {
            UserActionLogVO vo = new UserActionLogVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setActionType(item.getActionType());
            vo.setActionTypeDesc(OperationTypeEnum.getDesc(item.getActionType()));
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList());
        return result;
    }
}
