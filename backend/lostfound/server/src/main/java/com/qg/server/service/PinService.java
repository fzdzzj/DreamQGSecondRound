package com.qg.server.service;

import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;

public interface PinService {
    void apply(PinApplyDTO pinApplyDTO);

    void audit(PinAuditDTO pinAuditDTO);
}
