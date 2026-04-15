package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.pojo.dto.ApproveRequestDTO;
import com.qg.pojo.dto.BizClaimRequestDTO;
import com.qg.pojo.entity.BizClaimRequest;
import com.qg.pojo.vo.BizClaimRequestVO;

import java.util.List;

/**
 * 认领申请服务
 */
public interface BizClaimRequestService extends IService<BizClaimRequest> {

    /**
     * 失主发起认领申请
     *
     * @param request 认领申请请求
     */
    void createClaimRequest(BizClaimRequestDTO request);

    /**
     * 获取某物品的所有待审批申请
     *
     * @return 待审批申请列表
     */
    List<BizClaimRequestVO> getPendingRequests();

    /**
     * 拾取者审批（同意/拒绝/要求补充证据）
     *
     * @param dto 审批请求DTO
     */
    void approveRequest(Long id, ApproveRequestDTO dto);
}
