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

/**
 * 认领申请服务实现类
 * 实现认领申请服务接口
 * 包含创建、查询、审批认领申请等功能
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class BizClaimRequestServiceImpl extends ServiceImpl<BizClaimRequestDao, BizClaimRequest> implements BizClaimRequestService {

    private final NotificationService notificationService;
    private final BizItemDao itemDao;

    /**
     * 创建认领申请
     *
     * @param request 认领申请DTO
     * @return void
     * @throws BaseException 物品不存在异常
     * @throws BaseException 认领申请已存在异常
     * @throws BaseException 认领申请状态异常
     * @throws BaseException 认领申请创建失败异常
     *                       1. 检测物品是否存在
     *                       2. 创建认领申请
     *                       3.获取物品拥有者ID
     *                       4. 保存认领申请
     */
    @Override
    public void createClaimRequest(BizClaimRequestDTO request) {
        //1. 检测物品是否存在
        BizItem item = itemDao.selectById(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getId, request.getItemId())
        );
        if (item == null) {
            log.warn("物品 {} 不存在", request.getItemId());
            throw new BaseException(MessageConstant.ITEM_NOT_FOUND);
        }
        //2.创建认领申请
        BizClaimRequest bizClaimRequest = new BizClaimRequest();
        BeanUtils.copyProperties(request, bizClaimRequest);
        bizClaimRequest.setApplicantId(BaseContext.getCurrentId());
        bizClaimRequest.setStatus(BizClaimRequestStatusConstant.PENDING);
        //3. 获取物品拥有者ID
        Long ownerId = itemDao.selectOne(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getId, request.getItemId())
        ).getUserId();
        log.info("物品拥有者ID: {}", ownerId);
        bizClaimRequest.setOwnerId(ownerId);
        //4. 保存认领申请
        save(bizClaimRequest);
        log.info("创建认领申请成功: {}", bizClaimRequest);
    }

    /**
     * 获取待处理的认领申请
     *
     * @return List<BizClaimRequestVO>
     * 1. 获取当前用户ID
     * 2. 获取当前用户所有认领申请
     **/
    @Override
    public List<BizClaimRequestVO> getPendingRequests() {
        //1.获取当前用户的认领申请
        List<BizClaimRequest> requests = list(new LambdaQueryWrapper<BizClaimRequest>()
                .eq(BizClaimRequest::getStatus, BizClaimRequestStatusConstant.PENDING)
                .eq(BizClaimRequest::getOwnerId, BaseContext.getCurrentId())
                .or().eq(BizClaimRequest::getApplicantId, BaseContext.getCurrentId())
        );
        log.info("待处理的认领申请: 大小{}", requests.size());
        return requests.stream().map(request -> {
            BizClaimRequestVO vo = new BizClaimRequestVO();
            BeanUtils.copyProperties(request, vo);
            return vo;
        }).toList();
    }

    /**
     * 审批认领申请
     *
     * @param dto 审批信息
     * @return void
     * @throws BaseException 认领申请不存在异常
     * @throws BaseException 无权限审批异常
     * @throws BaseException 审批状态异常
     * @throws BaseException 创建通知失败异常
     *                       1. 获取认领申请
     *                       2. 获取当前用户ID
     *                       3. 权限检查：只有拾取者才能审批
     *                       4. 审批状态检查
     *                       5. 创建通知
     *                       6. 保存认领申请
     *                       7. 删除通知
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(Long id, ApproveRequestDTO dto) {
        //1. 获取认领申请
        BizClaimRequest request = getById(id);
        Long operatorId = BaseContext.getCurrentId();
        if (request == null) {
            log.warn("认领申请 {} 不存在", id);
            throw new BaseException(MessageConstant.CLAIM_REQUEST_ABSENT);
        }

        //2. 权限检查：只有拾取者才能审批
        if (!operatorId.equals(request.getOwnerId())) {
            log.warn("用户 {} 无权限审批认领申请 {}", operatorId, id);
            throw new BaseException(MessageConstant.NO_PERMISSION);
        }
        //3. 审批状态检查
        request.setStatus(dto.getStatus());
        if (BizClaimRequestStatusConstant.APPROVED.equals(dto.getStatus())) {
            request.setPickupCode(UUID.randomUUID().toString().substring(0, 8));
            log.info("生成取货码: {}", request.getPickupCode());
            notificationService.createNotification(request.getApplicantId(), id, MessageConstant.CLAIM_REQUEST_AUDIT_PASS + request.getPickupCode() + dto.getRemark());
        } else if (BizClaimRequestStatusConstant.REJECTED.equals(dto.getStatus())) {
            log.info("审批拒绝，通知申请人: {}", dto.getRemark());
            notificationService.createNotification(request.getApplicantId(), id, MessageConstant.CLAIM_REQUEST_AUDIT_REJECT + dto.getRemark());
        } else if (BizClaimRequestStatusConstant.MORE_INFO_REQUIRED.equals(dto.getStatus())) {
            log.info("审批需要更多信息，通知申请人: {}", dto.getRemark());
            notificationService.createNotification(request.getApplicantId(), id, MessageConstant.CLAIM_REQUEST_AUDIT_MORE_INFO_REQUIRED + dto.getRemark());
        }
        //4. 保存认领申请
        updateById(request);
        log.info("审批认领申请: {}", request);
    }
}
