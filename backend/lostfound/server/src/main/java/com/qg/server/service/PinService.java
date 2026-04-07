package com.qg.server.service;

import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;

public interface PinService {
    void apply(PinApplyDTO pinApplyDTO);
    void cancelPin(Long requestId);

    void audit(PinAuditDTO pinAuditDTO);
    void adminCancelPin(Long requestId, String reason);
}
