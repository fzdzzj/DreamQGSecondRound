package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.RiskHandleDTO;
import com.qg.pojo.entity.BizRiskEvent;
import com.qg.server.mapper.BizRiskEventDao;
import com.qg.server.service.RiskAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@Slf4j
@RequiredArgsConstructor
public class RiskAdminServiceImpl extends ServiceImpl<BizRiskEventDao, BizRiskEvent> implements RiskAdminService {
    private final BizRiskEventDao bizRiskEventDao;

    @Override
    public PageResult<BizRiskEvent> pageRiskEvents(int page, int pageSize, String handleStatus, String riskType) {
        Page<BizRiskEvent> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<BizRiskEvent> wrapper = new LambdaQueryWrapper<BizRiskEvent>();
        wrapper.eq(handleStatus != null, BizRiskEvent::getHandleStatus, handleStatus);
        wrapper.eq(riskType != null, BizRiskEvent::getRiskType, riskType);
        wrapper.orderByDesc(BizRiskEvent::getCreateTime);
        pageObj=this.page(pageObj,wrapper);
        return convert(pageObj);
    }

    private PageResult<BizRiskEvent> convert(Page<BizRiskEvent> pageObj) {
            return new PageResult<>(pageObj.getRecords(),pageObj.getTotal(),(int)pageObj.getCurrent(),(int)pageObj.getSize());
    }
    @Override
    public BizRiskEvent getRiskEventDetail(Long id) {
        return bizRiskEventDao.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRiskEvent(Long adminId, RiskHandleDTO dto) {
        BizRiskEvent event = bizRiskEventDao.selectById(dto.getRiskEventId());
        if (event == null) {
            throw new RuntimeException("风险事件不存在");
        }
        event.setHandleStatus(dto.getHandleStatus());
        event.setHandleRemark(dto.getHandleRemark());
        event.setHandledBy(adminId);
        event.setHandledTime(LocalDateTime.now());
        bizRiskEventDao.updateById(event);
    }
}
