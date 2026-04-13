package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.pojo.dto.ApproveRequestDTO;
import com.qg.pojo.dto.BizClaimRequestDTO;
import com.qg.pojo.entity.BizClaimRequest;
import com.qg.pojo.vo.BizClaimRequestVO;

import java.util.List;

public interface BizClaimRequestService extends IService<BizClaimRequest> {

    // 失主发起认领申请
    void createClaimRequest(BizClaimRequestDTO request);

    // 获取某物品的所有待审批申请
    List<BizClaimRequestVO> getPendingRequests();

    // 拾取者审批（同意/拒绝/要求补充证据）
    void approveRequest(ApproveRequestDTO dto);
}
