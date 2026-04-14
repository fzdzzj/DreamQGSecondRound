package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.RiskHandleDTO;
import com.qg.pojo.entity.BizRiskEvent;

public interface RiskAdminService extends IService<BizRiskEvent> {
    PageResult<BizRiskEvent> pageRiskEvents(int page, int pageSize, String handleStatus, String riskType);
    BizRiskEvent getRiskEventDetail(Long id);
    void handleRiskEvent(Long adminId, RiskHandleDTO dto);
}
