package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.PinRequestStatus;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizPinRequest;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizPinRequestDao;
import com.qg.server.service.PinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PinServiceImpl implements PinService {
    private final BizItemDao bizItemDao;
    private final BizPinRequestDao bizPinRequestDao;

    @Override
    public void apply(PinApplyDTO pinApplyDTO) {
        Long userId= BaseContext.getCurrentId();

        BizItem item=bizItemDao.selectById(pinApplyDTO.getItemId());
        if(item==null){
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        if(!item.getUserId().equals(userId)){
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        // 检查物品状态
        if(!item.getStatus().equals(BizItemStatus.OPEN)&&!item.getStatus().equals(BizItemStatus.MATCHED)){
            throw new BaseException(400, MessageConstant. ITEM_STATUS_INVALID);
        }

        // 不允许重复申请（未处理）
        Long count = bizPinRequestDao.selectCount(
                new LambdaQueryWrapper<BizPinRequest>()
                        .eq(BizPinRequest::getItemId, pinApplyDTO.getItemId())
                        .eq(BizPinRequest::getStatus, PinRequestStatus.PENDING)
        );

        if (count != null && count > 0) {
            throw new BaseException(400, "已有待审核的置顶申请");
        }
        BizPinRequest request = new BizPinRequest();
        request.setItemId(pinApplyDTO.getItemId());
        request.setApplicantId(userId);
        request.setReason(pinApplyDTO.getReason());
        request.setStatus(PinRequestStatus.PENDING);

        bizPinRequestDao.insert(request);



        log.info("提交置顶申请成功，itemId={}, userId={}", pinApplyDTO.getItemId(), userId);

    }

    @Override
    public void audit(PinAuditDTO pinAuditDTO) {

    }
}
