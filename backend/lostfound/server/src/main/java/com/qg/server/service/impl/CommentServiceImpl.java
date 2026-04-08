package com.qg.server.service.impl;

import com.qg.common.constant.MessageConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.entity.BizItem;
import com.qg.server.mapper.BizCommentDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final BizCommentDao bizCommentDao;
    private final BizItemDao bizItemDao;

    /**
     * 新增留言
     *
     * 说明：
     * 1. 仅允许对存在的物品留言
     * 2. 默认新增一级留言，parentId 为空时按 0 处理
     * 3. 留言成功后默认标记为未读，供后续“未读提示”功能使用
     *
     * @param commentAddDTO 留言参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentAddDTO commentAddDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增留言开始，userId={}, itemId={}", userId, commentAddDTO.getItemId());

        BizItem bizItem = bizItemDao.selectById(commentAddDTO.getItemId());
        if (bizItem == null) {
            log.warn("新增留言失败，物品不存在，itemId={}, userId={}", commentAddDTO.getItemId(), userId);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        BizComment bizComment = new BizComment();
        bizComment.setItemId(commentAddDTO.getItemId());
        bizComment.setUserId(userId);
        bizComment.setContent(commentAddDTO.getContent());
        bizComment.setParentId(commentAddDTO.getParentId() == null ? 0L : commentAddDTO.getParentId());
        bizComment.setIsRead(0);

        bizCommentDao.insert(bizComment);

        log.info("新增留言成功，commentId={}, userId={}, itemId={}",
                bizComment.getId(), userId, commentAddDTO.getItemId());
    }
}
