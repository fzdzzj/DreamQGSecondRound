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
/**
 * 拼单服务
 */
public interface PinService extends IService<BizPinRequest> {  // 继承 IService
    /**
     * 申请置顶
     * @param pinApplyDTO 申请置顶参数DTO
     */
    void apply(PinApplyDTO pinApplyDTO);
    /**
     * 取消置顶
     * @param requestId 置顶申请ID
     */
    void cancelPin(Long requestId);
/**
 * 审核置顶申请
 * @param pinAuditDTO 审核参数DTO
 */
    void audit(PinAuditDTO pinAuditDTO);

    /**
     * 查询页置顶申请
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态
     * @param applicantId 申请人ID
     * @param itemId 物品ID
     * @return 分页查询结果
     */
    PageResult<PinRequestStatVO> queryPinRequests(Integer pageNum, Integer pageSize, Integer status, Long applicantId, Long itemId);

    /**
     * 获取置顶申请详情
     * @param id 置顶申请ID
     * @return 置顶申请详情
     */
    PinRequestDetailVO getById(Long id);

    /**
     * 获取我的置顶申请列表
     * @return 置顶申请列表
     */
    List<PinRequestStatVO> myList();
}
