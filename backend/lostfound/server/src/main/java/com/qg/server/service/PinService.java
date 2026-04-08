package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.dto.PinRequestQueryDTO;
import com.qg.pojo.entity.BizPinRequest;
import com.qg.pojo.vo.PinRequestListVO;

import java.util.List;

public interface PinService {
    void apply(PinApplyDTO pinApplyDTO);
    void cancelPin(Long requestId);

    void audit(PinAuditDTO pinAuditDTO);

    PageResult<PinRequestListVO> queryPinRequests(PinRequestQueryDTO queryDTO);

    BizPinRequest getById(Long id);

    List<PinRequestListVO> myList();
}
