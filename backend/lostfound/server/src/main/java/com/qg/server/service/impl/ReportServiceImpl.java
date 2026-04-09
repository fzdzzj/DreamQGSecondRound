package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.ReportStatus;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.ReportStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.ViewNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<BizReportDao, BizReport> implements ReportService {  // 继承 ServiceImpl 和实现 ReportService

    private final BizItemDao itemDao;  // 物品数据访问层
    private final BizReportDao reportDao;  // 举报请求数据访问层

    /**
     * 提交举报
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReport(ReportDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户提交举报，itemId={}, userId={}", dto.getItemId(), userId);
        // 1. 校验物品是否存在
        BizItem item = itemDao.selectById(dto.getItemId());
        if (item == null) {
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 2. 防重复举报
        Long count = reportDao.selectCount(
                new LambdaQueryWrapper<BizReport>()
                        .eq(BizReport::getItemId, dto.getItemId())
                        .eq(BizReport::getReporterId, userId)
        );

        if (count != null && count > 0) {
            throw new BaseException(400, MessageConstant.REPORTED_ITEM);
        }

        // 3. 写入举报记录
        BizReport report = new BizReport();
        report.setItemId(dto.getItemId());
        report.setReporterId(userId);
        report.setReason(dto.getReason());
        report.setDetail(dto.getDetail());
        report.setStatus(ReportStatus.PENDING);

        save(report);  // 使用 IService 提供的 save 方法

        log.info("举报提交成功，itemId={}, userId={}", dto.getItemId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReport(ReportAuditDTO reportAuditDTO) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员审核举报，reportId={}, adminId={}", reportAuditDTO.getReportId(), adminId);
        BizReport report = getById((Serializable) reportAuditDTO.getReportId());  // 使用 IService 提供的 getById 方法
        if (report == null) {
            throw new AbsentException(MessageConstant.REPORT_NOT_FOUND);
        }
        if (!report.getStatus().equals(ReportStatus.PENDING)) {
            throw new BaseException(400, MessageConstant.REPORT_NOT_PENDING);
        }

        // 1. 更新举报记录
        report.setStatus(reportAuditDTO.getStatus());
        report.setAdminId(adminId);
        report.setAuditRemark(reportAuditDTO.getRemark());
        report.setAuditTime(LocalDateTime.now());

        updateById(report);  // 使用 IService 提供的 updateById 方法

        if (report.getStatus().equals(ReportStatus.APPROVED)) {
            log.info("举报审核通过，删除物品，itemId={}", report.getItemId());
            BizItem item = new BizItem();
            item.setId(report.getItemId());
            item.setStatus(BizItemStatus.REPORTED);
            itemDao.updateById(item);  // 更新物品状态为已举报
            itemDao.deleteById(report.getItemId());  // 删除物品

            log.info("物品删除成功，itemId={}", report.getItemId());
        }

        log.info("举报审核完成，reportId={}, result={}", reportAuditDTO.getReportId(), reportAuditDTO.getStatus());
    }

    @Override
    public PageResult<ReportListVO> list(ReportPageQueryDTO queryDTO) {
        Page<BizReport> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<BizReport> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getReporterId() != null) wrapper.eq(BizReport::getReporterId, queryDTO.getReporterId());
        if (StringUtils.isNotBlank(queryDTO.getStatus())) wrapper.eq(BizReport::getStatus, queryDTO.getStatus());
        page(page, wrapper);  // 使用 IService 提供的 page 方法
        return convertToVOPage(page);
    }

    @Override
    public ReportDetailVO getById(Long id) {
        String role = BaseContext.getCurrentRole();
        ReportDetailVO vo = new ReportDetailVO();
        if ("ADMIN".equals(role) || "SYSTEM".equals(role)) {
            BizReport report = getById((Serializable) id);  // 使用 IService 提供的 getById 方法
            if (report == null) {
                throw new AbsentException(MessageConstant.REPORT_NOT_FOUND);
            }
            BeanUtils.copyProperties(report, vo);
            vo.setStatusDesc(ReportStatusEnum.getDescByCode(report.getStatus()));
        } else if ("USER".equals(role)) {
            BizReport report = getById((Serializable) id);  // 使用 IService 提供的 getById 方法
            if (report == null) {
                throw new AbsentException(MessageConstant.REPORT_NOT_FOUND);
            }
            if (!report.getReporterId().equals(BaseContext.getCurrentId())) {
                throw new ViewNotAllowedException(MessageConstant.VIEW_NOT_ALLOWED);
            }
            BeanUtils.copyProperties(report, vo);
            vo.setStatusDesc(ReportStatusEnum.getDescByCode(report.getStatus()));
        }
        return vo;
    }

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
