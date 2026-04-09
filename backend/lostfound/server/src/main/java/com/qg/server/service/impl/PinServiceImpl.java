package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.PinConstant;
import com.qg.common.constant.PinRequestStatus;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.PinRequestStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.exception.ViewNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.dto.PinRequestQueryDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizPinRequest;
import com.qg.pojo.vo.PinRequestDetailVO;
import com.qg.pojo.vo.PinRequestStatVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizPinRequestDao;
import com.qg.server.service.PinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinServiceImpl extends ServiceImpl<BizPinRequestDao, BizPinRequest> implements PinService {  // 继承 ServiceImpl 和实现 PinService

    private final BizItemDao bizItemDao;  // 物品数据访问层
    private final BizPinRequestDao bizPinRequestDao;  // 置顶请求数据访问层

    @Override
    public void apply(PinApplyDTO pinApplyDTO) {
        Long userId = BaseContext.getCurrentId();

        BizItem item = bizItemDao.selectById(pinApplyDTO.getItemId());
        if (item == null) {
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        // 检查物品状态
        if (!item.getStatus().equals(BizItemStatus.OPEN) && !item.getStatus().equals(BizItemStatus.MATCHED)) {
            throw new BaseException(400, MessageConstant.ITEM_STATUS_INVALID);
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

        // 创建置顶请求
        BizPinRequest request = new BizPinRequest();
        request.setItemId(pinApplyDTO.getItemId());
        request.setApplicantId(userId);
        request.setReason(pinApplyDTO.getReason());
        request.setStatus(PinRequestStatus.PENDING);

        save(request); // 使用 IService 提供的 save 方法

        log.info("提交置顶申请成功，itemId={}, userId={}", pinApplyDTO.getItemId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPin(Long pinRequestId) {
        Long currentUserId = BaseContext.getCurrentId();
        String currentRole = BaseContext.getCurrentRole(); // RBAC 获取角色

        // 查询置顶申请
        BizPinRequest pinRequest = getById((Serializable) pinRequestId);  // 使用 IService 提供的 getById 方法
        if (pinRequest == null) {
            throw new AbsentException("置顶申请不存在");
        }

        // 用户角色逻辑：只能取消自己的申请，且状态为 PENDING
        if ("STUDENT".equals(currentRole)) {
            if (!pinRequest.getApplicantId().equals(currentUserId)) {
                throw new BaseException(403, "不能取消他人申请");
            }
            if (!PinRequestStatus.PENDING.equals(pinRequest.getStatus())) {
                throw new BaseException(400, "已处理的申请无法取消");
            }
            pinRequest.setStatus(PinRequestStatus.CANCELED);
            updateById(pinRequest);  // 使用 IService 提供的 updateById 方法
            log.info("学生取消自己的置顶申请，pinRequestId={}", pinRequestId);
            return;
        }

        // 管理员逻辑：可以取消任意申请
        if ("ADMIN".equals(currentRole) || "SYSTEM".equals(currentRole)) {
            if (PinRequestStatus.APPROVED.equals(pinRequest.getStatus())) {
                // 如果管理员取消已批准的申请，同时撤销物品置顶
                BizItem item = bizItemDao.selectById(pinRequest.getItemId());
                if (item != null) {
                    item.setIsPinned(0);
                    item.setPinExpireTime(null);
                    bizItemDao.updateById(item);
                }
            }
            pinRequest.setStatus(PinRequestStatus.CANCELED);
            updateById(pinRequest);  // 使用 IService 提供的 updateById 方法
            log.info("管理员取消置顶申请，pinRequestId={}", pinRequestId);
            return;
        }

        throw new BaseException(403, "无权限操作");
    }

    @Override
    public void audit(PinAuditDTO pinAuditDTO) {
        Long adminId = BaseContext.getCurrentId();

        BizPinRequest request = getById((Serializable) pinAuditDTO.getRequestId());  // 使用 IService 提供的 getById 方法
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

        updateById(update);  // 使用 IService 提供的 updateById 方法

        // 审核通过 → 真正置顶
        if (PinRequestStatus.APPROVED.equals(pinAuditDTO.getStatus())) {
            BizItem item = new BizItem();
            item.setId(request.getItemId());
            item.setIsPinned(1);
            item.setPinExpireTime(LocalDateTime.now().plusHours(PinConstant.PIN_EXPIRE_HOURS));

            bizItemDao.updateById(item);  // 使用 bizItemDao 的 updateById 方法
        }

        log.info("置顶审核完成，requestId={}, status={}", request.getId(), update.getStatus());
    }

    @Override
    public PageResult<PinRequestStatVO> queryPinRequests(PinRequestQueryDTO queryDTO) {
        Page<BizPinRequest> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<BizPinRequest> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getApplicantId() != null) wrapper.eq(BizPinRequest::getApplicantId, queryDTO.getApplicantId());
        if (StringUtils.isNotBlank(queryDTO.getStatus())) wrapper.eq(BizPinRequest::getStatus, queryDTO.getStatus());
        baseMapper.selectPage(page, wrapper);  // 使用 baseMapper 进行分页查询

        return convertToVOPage(page);
    }

    @Override
    public PinRequestDetailVO getById(Long id) {
        String role = BaseContext.getCurrentRole();
        BizPinRequest request = null;
        PinRequestDetailVO vo = new PinRequestDetailVO();
        if ("ADMIN".equals(role) || "SYSTEM".equals(role)) {
            request = getById((Serializable) id);  // 使用 IService 提供的 getById 方法
            if (request == null) {
                throw new AbsentException(MessageConstant.PIN_REQUEST_ABSENT);
            }

            BeanUtils.copyProperties(request, vo);
            vo.setStatusDesc(PinRequestStatusEnum.getDescByCode(request.getStatus()));
            return vo;
        }
        if ("STUDENT".equals(role)) {
            request = bizPinRequestDao.selectOne(new LambdaQueryWrapper<BizPinRequest>()
                    .eq(BizPinRequest::getId, id));
            if (request == null) {
                throw new AbsentException(MessageConstant.PIN_REQUEST_ABSENT);
            } else if (!request.getApplicantId().equals(BaseContext.getCurrentId())) {
                throw new ViewNotAllowedException(MessageConstant.VIEW_NOT_ALLOWED);
            }
            vo = new PinRequestDetailVO();
            BeanUtils.copyProperties(request, vo);
            vo.setStatusDesc(PinRequestStatusEnum.getDescByCode(request.getStatus()));
            return vo;
        }
        throw new ViewNotAllowedException(MessageConstant.VIEW_NOT_ALLOWED);
    }

    @Override
    public List<PinRequestStatVO> myList() {
        Long currentUserId = BaseContext.getCurrentId();
        List<PinRequestStatVO> list = bizPinRequestDao.selectList(new LambdaQueryWrapper<BizPinRequest>()
                        .eq(BizPinRequest::getApplicantId, currentUserId))
                .stream()
                .map(item -> {
                    PinRequestStatVO vo = new PinRequestStatVO();
                    BeanUtils.copyProperties(item, vo);
                    vo.setStatusDesc(PinRequestStatusEnum.getDescByCode(item.getStatus()));
                    return vo;
                })
                .collect(Collectors.toList());
        return list;
    }

    private PageResult<PinRequestStatVO> convertToVOPage(Page<BizPinRequest> page) {
        List<PinRequestStatVO> voList = page.getRecords().stream()
                .map(item -> {
                    PinRequestStatVO vo = new PinRequestStatVO();
                    BeanUtils.copyProperties(item, vo);
                    vo.setStatusDesc(PinRequestStatusEnum.getDescByCode(item.getStatus()));
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}
