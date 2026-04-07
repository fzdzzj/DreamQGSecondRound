package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.dto.PinRequestQueryDTO;
import com.qg.pojo.entity.BizPinRequest;

public interface PinService {
    void apply(PinApplyDTO pinApplyDTO);
    void cancelPin(Long requestId);

    void audit(PinAuditDTO pinAuditDTO);
    void adminCancelPin(Long requestId, String reason);

    PageResult<BizPinRequest> queryPinRequests(PinRequestQueryDTO queryDTO);
}
