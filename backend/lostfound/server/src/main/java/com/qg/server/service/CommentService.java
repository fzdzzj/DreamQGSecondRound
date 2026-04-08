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

}
