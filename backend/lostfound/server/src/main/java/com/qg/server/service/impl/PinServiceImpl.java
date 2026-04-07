package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.PinConstant;
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

import java.time.LocalDateTime;

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
    public void cancelPin(Long requestId) {
        Long userId = BaseContext.getCurrentId();
        var request = bizPinRequestDao.selectById(requestId);
        if (request == null) throw new AbsentException("申请不存在");
        if (!request.getApplicantId().equals(userId)) throw new BaseException(403, "无权限撤销");

        if (PinRequestStatus.PENDING.equals(request.getStatus())||PinRequestStatus.APPROVED.equals(request.getStatus())) {
            request.setStatus(PinRequestStatus.CANCELED);
            bizPinRequestDao.updateById(request);
            log.info("用户撤销置顶申请, requestId={}, userId={}", requestId, userId);
        } else {
            throw new BaseException(400, "无法撤销该申请");
        }
    }

    @Override
    public void audit(PinAuditDTO pinAuditDTO) {
        Long adminId = BaseContext.getCurrentId();

        BizPinRequest request = bizPinRequestDao.selectById(pinAuditDTO.getRequestId());
        if (request == null) {
            throw new AbsentException("置顶申请不存在");
        }

        if (!PinRequestStatus.PENDING.equals(request.getStatus())) {
            throw new BaseException(400, "该申请已处理");
        }

        // 更新申请
        BizPinRequest update = new BizPinRequest();
        update.setId(request.getId());
        update.setStatus(pinAuditDTO.getStatus());
        update.setAuditAdminId(adminId);
        update.setAuditRemark(pinAuditDTO.getRemark());
        update.setAuditTime(LocalDateTime.now());

        bizPinRequestDao.updateById(update);

        // 审核通过 → 真正置顶
        if (PinRequestStatus.APPROVED.equals(pinAuditDTO.getStatus())) {
            BizItem item = new BizItem();
            item.setId(request.getItemId());
            item.setIsPinned(1);
            item.setPinExpireTime(LocalDateTime.now().plusHours(PinConstant.PIN_EXPIRE_HOURS));

            bizItemDao.updateById(item);
        }

        log.info("置顶审核完成，requestId={}, status={}", request.getId(), update.getStatus());

    }
}
