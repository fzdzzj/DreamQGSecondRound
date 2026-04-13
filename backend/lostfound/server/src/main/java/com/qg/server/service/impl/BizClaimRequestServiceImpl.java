package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.BizClaimRequestStatusConstant;
import com.qg.common.constant.MessageConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.BaseException;
import com.qg.pojo.dto.ApproveRequestDTO;
import com.qg.pojo.dto.BizClaimRequestDTO;
import com.qg.pojo.entity.BizClaimRequest;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.vo.BizClaimRequestVO;
import com.qg.server.mapper.BizClaimRequestDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.BizClaimRequestService;
import com.qg.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizClaimRequestServiceImpl extends ServiceImpl<BizClaimRequestDao, BizClaimRequest> implements BizClaimRequestService {

    private final BizClaimRequestDao claimRequestDao;
    private final NotificationService notificationService;
    private final BizItemDao itemDao;

    @Override
    @Transactional
    public BizClaimRequestVO createClaimRequest(BizClaimRequestDTO request) {
        BizClaimRequest bizClaimRequest = new BizClaimRequest();
        BeanUtils.copyProperties(request, bizClaimRequest);
        bizClaimRequest.setStatus(BizClaimRequestStatusConstant.PENDING);
        Long ownerId = itemDao.selectOne(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getId, request.getItemId())
        ).getUserId();
        bizClaimRequest.setOwnerId(ownerId);
        save(bizClaimRequest);
        BizClaimRequestVO vo = new BizClaimRequestVO();
        BeanUtils.copyProperties(bizClaimRequest, vo);
        log.info("创建认领申请: {}", vo);
        return vo;
    }

    @Override
    public List<BizClaimRequestVO> getPendingRequestsByItem(Long itemId) {
        List<BizClaimRequest> requests = list(new LambdaQueryWrapper<BizClaimRequest>()
                .eq(BizClaimRequest::getItemId, itemId)
                .eq(BizClaimRequest::getStatus, BizClaimRequestStatusConstant.PENDING)
                .eq(BizClaimRequest::getOwnerId, BaseContext.getCurrentId())
                .or().eq(BizClaimRequest::getApplicantId, BaseContext.getCurrentId())
    );
        return requests.stream().map(request -> {
            BizClaimRequestVO vo = new BizClaimRequestVO();
            BeanUtils.copyProperties(request, vo);
            return vo;
        }).toList();
    }



    @Override
    @Transactional
    public void approveRequest(ApproveRequestDTO dto) {
        BizClaimRequest request = getById(dto.getRequestId());
        Long operatorId = BaseContext.getCurrentId();
        if (request == null) {
            log.warn("认领申请 {} 不存在", dto.getRequestId());
            throw new BaseException(MessageConstant .CLAIM_REQUEST_ABSENT);
        }

        // 权限检查：只有拾取者才能审批
        if (!operatorId.equals(request.getOwnerId())) {
            log.warn("用户 {} 无权限审批认领申请 {}", operatorId, dto.getRequestId());
            throw new BaseException(MessageConstant.NO_PERMISSION);
        }

        request.setStatus(dto.getStatus());
        if (BizClaimRequestStatusConstant.APPROVED.equals(dto.getStatus())) {
                request.setPickupCode(UUID.randomUUID().toString().substring(0, 8));
            notificationService.createNotification(request.getApplicantId(), dto.getRequestId(), MessageConstant .CLAIM_REQUEST_AUDIT_PASS+request.getPickupCode()+ dto.getRemark());
        }else if(BizClaimRequestStatusConstant.REJECTED.equals(dto.getStatus())){
            notificationService.createNotification(request.getApplicantId(), dto.getRequestId(), MessageConstant .CLAIM_REQUEST_AUDIT_REJECT+dto.getRemark());
        }else if(BizClaimRequestStatusConstant.MORE_INFO_REQUIRED.equals(dto.getStatus())){
            notificationService.createNotification(request.getApplicantId(), dto.getRequestId(), MessageConstant .CLAIM_REQUEST_AUDIT_MORE_INFO_REQUIRED+dto.getRemark());
        }
        updateById(request);
        log.info("审批认领申请: {}", request);
    }
}
