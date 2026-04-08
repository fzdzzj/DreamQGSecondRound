package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.vo.CommentVO;

public interface CommentService {
    void addComment(CommentAddDTO commentAddDTO);
    PageResult<CommentVO>getCommentList(Long itemId,int pageNum,int pageSize);
}
