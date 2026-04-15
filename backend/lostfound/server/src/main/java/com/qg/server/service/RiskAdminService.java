package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.RiskHandleDTO;
import com.qg.pojo.entity.BizRiskEvent;

/**
 * 风险事件服务
 */
public interface RiskAdminService extends IService<BizRiskEvent> {
    /**
     * 获取风险事件列表
     *
     * @param page         页码
     * @param pageSize     每页数量
     * @param handleStatus 处理状态
     * @param riskType     风险类型
     * @return 分页查询结果
     */
    PageResult<BizRiskEvent> pageRiskEvents(int page, int pageSize, String handleStatus, String riskType);

    /**
     * 获取风险事件详情
     *
     * @param id 风险事件ID
     * @return 风险事件详情
     */
    BizRiskEvent getRiskEventDetail(Long id);

    /**
     * 处理风险事件
     *
     * @param adminId 管理员ID
     * @param dto     处理参数
     */
    void handleRiskEvent(Long adminId, RiskHandleDTO dto);
}
