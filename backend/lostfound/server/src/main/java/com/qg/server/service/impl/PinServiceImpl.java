package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.*;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.PinRequestStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.exception.ViewNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizPinRequest;
import com.qg.pojo.vo.PinRequestDetailVO;
import com.qg.pojo.vo.PinRequestStatVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizPinRequestDao;
import com.qg.server.service.NotificationService;
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

/**
 * 置顶服务实现类
 * 置顶申请 管理物品置顶申请等
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PinServiceImpl extends ServiceImpl<BizPinRequestDao, BizPinRequest> implements PinService {  // 继承 ServiceImpl 和实现 PinService

    private final BizItemDao bizItemDao;
    private final BizPinRequestDao bizPinRequestDao;
    private final NotificationService notificationService;

    /**
     * 申请置顶
     *
     * @param pinApplyDTO 置顶申请DTO
     *                    1. 校验物品是否存在
     *                    2. 校验物品状态是否为开放或匹配中
     *                    3. 不允许重复申请（未处理）
     *                    4. 创建置顶请求
     *                    5. 保存置顶请求
     *                    6. 发送通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(PinApplyDTO pinApplyDTO) {
        Long userId = BaseContext.getCurrentId();
        // 1.校验物品是否存在
        BizItem item = bizItemDao.selectById(pinApplyDTO.getItemId());
        if (item == null) {
            log.warn("物品{}不存在", pinApplyDTO.getItemId());
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            log.warn("物品{}的用户{}不是当前用户{}", pinApplyDTO.getItemId(), item.getUserId(), userId);
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        // 2.检查物品状态是否为开放或匹配中
        if (!item.getStatus().equals(BizItemStatusConstant.OPEN) && !item.getStatus().equals(BizItemStatusConstant.MATCHED)) {
            log.warn("物品{}状态不是开放或匹配中", pinApplyDTO.getItemId());
            throw new BaseException(400, MessageConstant.ITEM_STATUS_INVALID);
        }

        // 3.不允许重复申请（未处理）
        Long count = bizPinRequestDao.selectCount(
                new LambdaQueryWrapper<BizPinRequest>()
                        .eq(BizPinRequest::getItemId, pinApplyDTO.getItemId())
                        .eq(BizPinRequest::getStatus, PinRequestStatusConstant.PENDING)
        );
        log.warn("物品{}有{}个待审核的置顶申请", pinApplyDTO.getItemId(), count);

        if (count != null && count > 0) {
            throw new BaseException(400, "已有待审核的置顶申请");
        }

        // 4.创建置顶请求
        BizPinRequest request = new BizPinRequest();
        request.setItemId(pinApplyDTO.getItemId());
        request.setApplicantId(userId);
        request.setReason(pinApplyDTO.getReason());
        request.setStatus(PinRequestStatusConstant.PENDING);
        // 5.保存置顶请求
        save(request); // 使用 IService 提供的 save 方法
        log.info("创建置顶请求成功，itemId={}, userId={}", pinApplyDTO.getItemId(), userId);
        // 6.发送通知
        notificationService.createNotification(userId, pinApplyDTO.getItemId(), "您申请物品置顶的申请已提交");
        notificationService.createNotification(item.getUserId(), pinApplyDTO.getItemId(), "有新的置顶申请");
        log.info("提交置顶申请成功，itemId={}, userId={}", pinApplyDTO.getItemId(), userId);
    }

    /**
     * 取消置顶申请
     *
     * @param pinRequestId 用户处理申请ID
     *                     1. 校验置顶申请是否存在
     *                     2. 校验置顶申请状态是否为待处理
     *                     3. 删除置顶申请
     *                     4. 撤销物品置顶
     *                     5. 发送通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPin(Long pinRequestId) {
        Long currentUserId = BaseContext.getCurrentId();
        String currentRole = BaseContext.getCurrentRole(); // RBAC 获取角色
        log.info("取消置顶申请，userId={}, pinRequestId={}", currentUserId, pinRequestId);
        // 1.校验置顶申请是否存在
        BizPinRequest pinRequest = getById((Serializable) pinRequestId);  // 使用 IService 提供的 getById 方法
        if (pinRequest == null) {
            throw new AbsentException("置顶申请不存在");
        }
        // 2.校验置顶申请状态是否为待处理
        // 用户角色逻辑：只能取消自己的申请，且状态为 PENDING
        if (RoleConstant.USER.equals(currentRole)) {
            if (!pinRequest.getApplicantId().equals(currentUserId)) {
                log.warn("用户{}不能取消别人的申请", currentUserId);
                throw new BaseException(403, "不能取消他人申请");
            }
            if (!PinRequestStatusConstant.PENDING.equals(pinRequest.getStatus())) {
                log.warn("置顶申请{}状态不是待处理", pinRequestId);
                throw new BaseException(400, "已处理的申请无法取消");
            }
            // 4.删除置顶申请
            pinRequest.setStatus(PinRequestStatusConstant.CANCELED);
            updateById(pinRequest);
            log.info("学生取消自己的置顶申请，pinRequestId={}", pinRequestId);
            return;
        }
        log.warn("用户{}没有权限取消置顶申请", currentUserId);
        throw new BaseException(403, MessageConstant.NO_PERMISSION);
    }

    /**
     * 审核置顶申请
     *
     * @param pinAuditDTO 审核信息
     *                    1. 校验置顶申请是否存在
     *                    2. 校验置顶申请状态是否为待处理
     *                    3. 更新置顶申请
     *                    4. 操作物品置顶
     *                    5. 发送通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(PinAuditDTO pinAuditDTO) {
        Long adminId = BaseContext.getCurrentId();
        // 1. 校验置顶申请是否存在
        log.info("审核置顶申请，requestId={}", pinAuditDTO.getRequestId());
        BizPinRequest request = getById((Serializable) pinAuditDTO.getRequestId());  // 使用 IService 提供的 getById 方法
        if (request == null) {
            log.warn("置顶申请{}不存在", pinAuditDTO.getRequestId());
            throw new AbsentException("置顶申请不存在");
        }
        // 2. 校验置顶申请状态是否为待处理
        if (!PinRequestStatusConstant.PENDING.equals(request.getStatus())) {
            throw new BaseException(400, "该申请已处理");
        }

        // 3. 更新置顶申请状态
        BizPinRequest update = new BizPinRequest();
        update.setId(request.getId());
        update.setStatus(pinAuditDTO.getStatus());
        update.setAuditAdminId(adminId);
        update.setAuditRemark(pinAuditDTO.getRemark());
        update.setAuditTime(LocalDateTime.now());

        updateById(update);  // 使用 IService 提供的 updateById 方法
        log.info("更新置顶申请状态，requestId={}, status={}", pinAuditDTO.getRequestId(), pinAuditDTO.getStatus());
        // 4. 操作物品置顶
        if (PinRequestStatusConstant.APPROVED.equals(pinAuditDTO.getStatus())) {
            BizItem item = new BizItem();
            item.setId(request.getItemId());
            item.setIsPinned(PinConstant.PINNED);
            item.setPinExpireTime(LocalDateTime.now().plusHours(PinConstant.PIN_EXPIRE_HOURS));

            bizItemDao.updateById(item);  // 使用 bizItemDao 的 updateById 方法
            log.info("物品{}的置顶状态从未置顶{}更改为已置顶{}", request.getItemId(), PinConstant.NOT_PINNED, PinConstant.PINNED);
        }
        // 5. 发送通知
        notificationService.createNotification(request.getApplicantId(), request.getId(), "您的置顶申请状态已"+PinRequestStatusEnum.getDescByCode(update.getStatus()));

        log.info("置顶审核完成，requestId={}, status={}", request.getId(), update.getStatus());
    }

    /**
     * 查询置顶申请列表
     *
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @param status      状态
     * @param applicantId 申请人ID
     * @param itemId      物品ID
     * @return 分页查询结果
     * 1. 校验参数
     * 2. 查询置顶申请列表
     * 3. 转换为 VO
     */
    @Override
    public PageResult<PinRequestStatVO> queryPinRequests(Integer pageNum, Integer pageSize, Integer status, Long applicantId, Long itemId) {
        if (pageNum == null || pageNum < 1) {
            pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
        }
        //1.构建查询参数
        Page<BizPinRequest> page = new Page<>(pageNum, pageSize);
        //2.构建查询条件
        LambdaQueryWrapper<BizPinRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BizPinRequest::getCreateTime);
        if(itemId != null) wrapper.eq(BizPinRequest::getItemId, itemId);
        if (applicantId != null) wrapper.eq(BizPinRequest::getApplicantId, applicantId);
        if (status != null && StringUtils.isNotBlank(status.toString())) wrapper.eq(BizPinRequest::getStatus, status);
        //3.查询置顶申请列表
        baseMapper.selectPage(page, wrapper);
        //4.转换为 VO
        log.info("查询置顶申请列表结束,总条数：{}", page.getTotal());
        return convertToVOPage(page);
    }

    /**
     * 获取置顶申请详情
     *
     * @param id 置顶申请id
     *           1. 校验参数
     *           2. 获取置顶申请详情
     *           3. 转换为 VO
     *           <p>
     *           管理员逻辑：可以查看任意申请详情
     *           1.检测置顶申请是否存在
     *           2.转换为 VO
     *           <p>
     *           用户逻辑：只能查看自己的申请详情
     *           1.检测置顶申请是否存在
     *           2.校验置顶申请是否为当前用户
     *           3.转换为 VO
     */
    @Override
    public PinRequestDetailVO getById(Long id) {
        String role = BaseContext.getCurrentRole();
        BizPinRequest request;
        PinRequestDetailVO vo = new PinRequestDetailVO();
        // 管理员逻辑：可以查看任意申请详情
        if (RoleConstant.ADMIN.equals(role) || RoleConstant.SYSTEM.equals(role)) {
            request = getById((Serializable) id);  // 使用 IService 提供的 getById 方法
            // 1.校验置顶申请是否存在
            if (request == null) {
                log.warn("置顶申请{}不存在", id);
                throw new AbsentException(MessageConstant.PIN_REQUEST_ABSENT);
            }
            // 2.转换为 VO
            BeanUtils.copyProperties(request, vo);
            vo.setStatusDesc(PinRequestStatusEnum.getDescByCode(request.getStatus()));
            return vo;
        }
        // 用户逻辑：只能查看自己的申请详情
        if (RoleConstant.USER.equals(role)) {
            // 1.校验置顶申请是否存在
            request = bizPinRequestDao.selectOne(new LambdaQueryWrapper<BizPinRequest>()
                    .eq(BizPinRequest::getId, id));
            if (request == null) {
                log.warn("置顶申请{}不存在", id);
                throw new AbsentException(MessageConstant.PIN_REQUEST_ABSENT);
                // 2.校验置顶申请是否为当前用户
            } else if (!request.getApplicantId().equals(BaseContext.getCurrentId())) {
                log.warn("用户无查看权限，requestId={}", id);
                throw new ViewNotAllowedException(MessageConstant.VIEW_NOT_ALLOWED);
            }

            // 3.转换为 VO
            vo = new PinRequestDetailVO();
            BeanUtils.copyProperties(request, vo);
            vo.setStatusDesc(PinRequestStatusEnum.getDescByCode(request.getStatus()));
            return vo;
        }
        throw new ViewNotAllowedException(MessageConstant.VIEW_NOT_ALLOWED);
    }

    /**
     * 获取当前用户置顶申请列表
     *
     * @return 置顶申请列表
     */
    @Override
    public List<PinRequestStatVO> myList() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取当前用户{}的置顶申请列表开始", currentUserId);
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

    /**
     * 清除过时的置顶申请
     */
    @Override
    public void clearPinRequests() {
        log.info("清除过时的置顶申请开始");
        bizPinRequestDao.delete(new LambdaQueryWrapper<BizPinRequest>()
                .lt(BizPinRequest::getCreateTime, LocalDateTime.now().minusDays(1)));
        log.info("清除过时的置顶申请完成");
    }


    /**
     * 转换为 VO
     *
     * @param page 置顶申请列表
     * @return 置顶申请列表
     */
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
