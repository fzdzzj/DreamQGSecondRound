package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.dto.PinRequestQueryDTO;
import com.qg.pojo.entity.BizPinRequest;
import com.qg.pojo.vo.PinRequestDetailVO;
import com.qg.pojo.vo.PinRequestStatVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PinService extends IService<BizPinRequest> {  // 继承 IService

    void apply(PinApplyDTO pinApplyDTO);

    void cancelPin(Long requestId);

    void audit(PinAuditDTO pinAuditDTO);

    PageResult<PinRequestStatVO> queryPinRequests(PinRequestQueryDTO queryDTO);

    PinRequestDetailVO getById(Long id);

    List<PinRequestStatVO> myList();
}
