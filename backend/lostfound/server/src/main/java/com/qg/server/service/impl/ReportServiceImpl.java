package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.*;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.ReportStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.ViewNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizReport;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizReportDao;
import com.qg.server.service.ReportService;
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
 * 举报服务实现类
 * 提供举报相关功能，如提交举报、审核举报、查询举报列表等。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<BizReportDao, BizReport> implements ReportService {  // 继承 ServiceImpl 和实现 ReportService

    private final BizItemDao itemDao;  // 物品数据访问层
    private final BizReportDao reportDao;  // 举报请求数据访问层

    /**
     * 提交举报
     *
     * @param dto 举报数据传输对象
     *            1. 校验物品是否存在
     *            2. 防重复举报
     *            3. 写入举报记录
     */
    @Override
    public void submitReport(ReportDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户提交举报，itemId={}, userId={}", dto.getItemId(), userId);
        // 1. 校验物品是否存在
        BizItem item = itemDao.selectById(dto.getItemId());
        if (item == null) {
            log.info("物品不存在，itemId={}", dto.getItemId());
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 2. 防重复举报
        Long count = reportDao.selectCount(
                new LambdaQueryWrapper<BizReport>()
                        .eq(BizReport::getItemId, dto.getItemId())
                        .eq(BizReport::getReporterId, userId)
        );

        if (count != null && count > 0) {
            log.info("用户已举报过该物品，itemId={}, userId={}", dto.getItemId(), userId);
            throw new BaseException(400, MessageConstant.REPORTED_ITEM);
        }

        // 3. 写入举报记录
        BizReport report = new BizReport();
        report.setItemId(dto.getItemId());
        report.setReporterId(userId);
        report.setReason(dto.getReason());
        report.setDetail(dto.getDetail());
        report.setStatus(ReportStatusConstant.PENDING);

        save(report);  // 使用 IService 提供的 save 方法

        log.info("举报提交成功，itemId={}, userId={}", dto.getItemId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReport(Long reportId, ReportAuditDTO reportAuditDTO) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员审核举报，reportId={}, adminId={}", reportId, adminId);
        // 1. 校验举报记录是否存在
        BizReport report = getById((Serializable) reportId);  // 使用 IService 提供的 getById 方法
        if (report == null) {
            log.info("举报记录不存在，reportId={}", reportId);
            throw new AbsentException(MessageConstant.REPORT_NOT_FOUND);
        }
        // 2. 校验举报记录状态为待审核
        if (!report.getStatus().equals(ReportStatusConstant.PENDING)) {
            log.info("举报记录状态不是待审核，reportId={}", reportId);
            throw new BaseException(400, MessageConstant.REPORT_NOT_PENDING);
        }

        // 3. 更新举报记录状态
        report.setStatus(reportAuditDTO.getStatus());
        report.setAdminId(adminId);
        report.setAuditRemark(reportAuditDTO.getRemark());
        report.setAuditTime(LocalDateTime.now());

        updateById(report);  // 使用 IService 提供的 updateById 方法
        // 4. 校验举报记录状态为已通过
        if (report.getStatus().equals(ReportStatusConstant.APPROVED)) {
            log.info("举报审核通过，删除物品，itemId={}", report.getItemId());
            BizItem item = new BizItem();
            item.setId(report.getItemId());
            item.setStatus(BizItemStatusConstant.REPORTED);
            itemDao.updateById(item);  // 更新物品状态为已举报
            itemDao.deleteById(report.getItemId());  // 删除物品
            log.info("物品删除成功，itemId={}", report.getItemId());
        }

        log.info("举报审核完成，reportId={}, result={}", reportId, reportAuditDTO.getStatus());
    }

    /**
     * 分页查询举报列表
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param status     举报状态
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param reporterId 举报人ID
     * @param itemId     物品ID
     * @return 1. 分页对象
     * 2. 条件构造器
     * 3. 分页查询
     * 4. 转换为VO并返回
     */
    @Override
    public PageResult<ReportListVO> page(Integer pageNum, Integer pageSize, Integer status, LocalDateTime startTime, LocalDateTime endTime, Long reporterId, Long itemId) {
        if (pageNum == null || pageNum < 1) {
            pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
        }
        // 1. 分页对象
        Page<BizReport> page = new Page<>(pageNum, pageSize);
        log.info("分页查询举报列表，pageNum={}, pageSize={}", pageNum, pageSize);

        // 2. 条件构造器
        LambdaQueryWrapper<BizReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(reporterId != null, BizReport::getReporterId, reporterId)
                .eq(status != null, BizReport::getStatus, status)
                .eq(itemId != null, BizReport::getItemId, itemId)
                .ge(startTime != null, BizReport::getCreateTime, startTime)
                .le(endTime != null, BizReport::getCreateTime, endTime)
                .orderByDesc(BizReport::getCreateTime); // 按时间倒序更合理

        // 3. 分页查询
        page(page, wrapper);
        log.info("查询结果，total={}, records={}", page.getTotal(), page.getRecords());

        // 4. 转换VO并返回
        return convertToVOPage(page);
    }

    /**
     * 根据ID查询举报记录详情
     *
     * @param id 举报记录ID
     * @return 举报记录详情VO
     * 1. 校验角色是否为管理员或系统用户
     * 2. 校验举报记录是否存在
     * 3. 校验用户是否为举报记录的举报人
     * 4. 转换VO
     */
    @Override
    public ReportDetailVO getById(Long id) {
        String role = BaseContext.getCurrentRole();
        ReportDetailVO vo = new ReportDetailVO();
        //1. 校验角色是否为管理员或系统用户
        if (RoleConstant.ADMIN.equals(role) || RoleConstant.SYSTEM.equals(role)) {
            BizReport report = getById((Serializable) id);
            // 2. 校验举报记录是否存在
            if (report == null) {
                throw new AbsentException(MessageConstant.REPORT_NOT_FOUND);
            }
            //3. 转换VO
            BeanUtils.copyProperties(report, vo);
            vo.setStatusDesc(ReportStatusEnum.getDescByCode(report.getStatus()));
        } else if (RoleConstant.USER.equals(role)) {
            BizReport report = getById((Serializable) id);
            // 2. 校验举报记录是否存在
            if (report == null) {
                throw new AbsentException(MessageConstant.REPORT_NOT_FOUND);
            }
            // 3. 校验用户是否为举报记录的举报人
            if (!report.getReporterId().equals(BaseContext.getCurrentId())) {
                log.warn("用户无权限查看该举报记录，userId={}, reportId={}", BaseContext.getCurrentId(), id);
                throw new ViewNotAllowedException(MessageConstant.VIEW_NOT_ALLOWED);
            }
            BeanUtils.copyProperties(report, vo);
            vo.setStatusDesc(ReportStatusEnum.getDescByCode(report.getStatus()));
        }
        log.info("查询结果，report={}", vo);
        return vo;
    }

    /**
     * 转换VO并返回
     *
     * @param page 分页对象
     * @return 分页VO对象
     */
    private PageResult<ReportListVO> convertToVOPage(Page<BizReport> page) {
        List<BizReport> records = page.getRecords();
        List<ReportListVO> vos = records.stream().map(report -> {
            ReportListVO vo = new ReportListVO();
            BeanUtils.copyProperties(report, vo);
            vo.setStatusDesc(ReportStatusEnum.getDescByCode(report.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(vos, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}
