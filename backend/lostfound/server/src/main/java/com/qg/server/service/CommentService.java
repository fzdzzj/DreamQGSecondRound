package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.vo.CommentDetailVO;
import com.qg.pojo.vo.CommentStatVO;

public interface CommentService {
    void addComment(CommentAddDTO commentAddDTO);


    PageResult<CommentStatVO> getCommentList(Long itemId, Integer pageNum, Integer pageSize);

    /**
     * 删除留言
     *
     * @param commentId 留言ID
     */
    void deleteComment(Long commentId);

    /**
     * 根据留言ID获取留言详情
     *
     * @param commentId 留言ID
     * @return 留言详情
     */
    CommentDetailVO getCommentDetail(Long commentId);

    /**
     * 更新留言为已读
     *
     * @param commentId 留言ID
     */
    void markAsRead(Long commentId);

    /**
     * 获取物品下的未读留言数量
     *
     * @param itemId 物品ID
     * @return 未读留言数量
     */
    Long getUnreadCount(Long itemId);

    /**
     * 获取用户的未读留言数量
     *
     * @param userId 用户ID
     * @return 未读留言数量
     */
    Long getUserUnreadCount(Long userId);

    /**
     * 回复留言
     *
     * @param commentAddDTO 留言添加的参数
     */
    void replyComment(CommentAddDTO commentAddDTO);
}
