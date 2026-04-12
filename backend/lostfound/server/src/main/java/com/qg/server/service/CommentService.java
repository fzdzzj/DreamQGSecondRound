package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.vo.CommentDetailVO;
import com.qg.pojo.vo.CommentStatVO;

public interface CommentService extends IService<BizComment> {

    /**
     * 新增留言
     *
     * @param commentAddDTO 留言参数
     */
    void addComment(CommentAddDTO commentAddDTO);

    /**
     * 获取物品留言列表（分页）
     *
     * @param itemId 物品ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    PageResult<CommentStatVO> getCommentList(Long itemId, Integer pageNum, Integer pageSize);

    /**
     * 删除留言（逻辑删除）
     *
     * @param commentId 留言ID
     */
    void deleteComment(Long commentId);

    /**
     * 获取留言详情
     *
     * @param commentId 留言ID
     * @return 留言详细信息
     */
    CommentDetailVO getCommentDetail(Long commentId);

    /**
     * 标记留言为已读
     *
     * @param commentId 留言ID
     */
    void markAsRead(Long commentId);

    /**
     * 获取物品下未读留言数量
     *
     * @param itemId 物品ID
     * @return 未读留言数量
     */
    Long getUnreadCount(Long itemId);

    /**
     * 获取用户未读留言数量
     *
     * @param userId 用户ID
     * @return 未读留言数量
     */
    Long getUserUnreadCount(Long userId);

}
