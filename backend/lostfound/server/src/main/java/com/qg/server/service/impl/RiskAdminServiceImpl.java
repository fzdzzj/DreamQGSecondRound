package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.DefaultPageConstant;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.RiskHandleDTO;
import com.qg.pojo.entity.BizRiskEvent;
import com.qg.server.mapper.BizRiskEventDao;
import com.qg.server.service.RiskAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 风险事件管理员服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RiskAdminServiceImpl extends ServiceImpl<BizRiskEventDao, BizRiskEvent> implements RiskAdminService {
    private final BizRiskEventDao bizRiskEventDao;

    /**
     * 分页查询风险事件
     *
     * @param page         当前页码
     * @param pageSize     每页数量
     * @param handleStatus 处理状态
     * @param riskType     风险类型
     * @return 分页结果
     * <p>
     * 1. 参数校验
     * 2. 构建查询条件
     * 3. 执行查询
     */
    @Override
    public PageResult<BizRiskEvent> pageRiskEvents(int page, int pageSize, String handleStatus, String riskType) {
        // 1.参数校验
        if (page <= 0) {
            page = DefaultPageConstant.DEFAULT_PAGE_NUM;
        }
        if (pageSize <= 0) {
            pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
        }
        Page<BizRiskEvent> pageObj = new Page<>(page, pageSize);
        // 2.构建查询条件
        LambdaQueryWrapper<BizRiskEvent> wrapper = new LambdaQueryWrapper<BizRiskEvent>();
        wrapper.eq(handleStatus != null, BizRiskEvent::getHandleStatus, handleStatus);
        wrapper.eq(riskType != null, BizRiskEvent::getRiskType, riskType);
        wrapper.orderByDesc(BizRiskEvent::getCreateTime);
        // 3.执行查询
        pageObj = this.page(pageObj, wrapper);
        return convert(pageObj);
    }

    /**
     * 查询风险事件详情
     *
     * @param id 风险事件ID
     * @return 风险事件详情
     */
    @Override
    public BizRiskEvent getRiskEventDetail(Long id) {
        log.info("查询风险事件详情，id={}", id);
        BizRiskEvent event = bizRiskEventDao.selectById(id);
        log.info("查询风险事件详情成功，id={},event={}", id, event);
        return event;
    }

    /**
     * 处理风险事件
     *
     * @param adminId 管理员ID
     * @param dto     处理信息
     */
    @Override
    public void handleRiskEvent(Long adminId, RiskHandleDTO dto) {
        BizRiskEvent event = bizRiskEventDao.selectById(dto.getRiskEventId());
        log.info("处理风险事件，adminId={},dto={}", adminId, dto);
        if (event == null) {
            throw new RuntimeException("风险事件不存在");
        }
        event.setHandleStatus(dto.getHandleStatus());
        event.setHandleRemark(dto.getHandleRemark());
        event.setHandledBy(adminId);
        event.setHandledTime(LocalDateTime.now());
        bizRiskEventDao.updateById(event);
        log.info("处理风险事件成功，adminId={},dto={},event={}", adminId, dto, event);
    }

    /**
     * 转换分页结果
     *
     * @param pageObj 分页对象
     * @return 分页结果
     */
    private PageResult<BizRiskEvent> convert(Page<BizRiskEvent> pageObj) {
        return new PageResult<>(pageObj.getRecords(), pageObj.getTotal(), (int) pageObj.getCurrent(), (int) pageObj.getSize());
    }
}
