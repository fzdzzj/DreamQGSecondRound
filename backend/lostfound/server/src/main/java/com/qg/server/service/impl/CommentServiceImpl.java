package com.qg.server.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.constant.MessageConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.CommentDetailVO;
import com.qg.pojo.vo.CommentStatVO;
import com.qg.server.mapper.BizCommentDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.UserDao;
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
    private final UserDao UserDao;

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

    @Override
    public PageResult<CommentStatVO> getCommentList(Long itemId, Integer pageNum, Integer pageSize) {
        log.info("查询物品留言，itemId={}, pageNum={}, pageSize={}", itemId, pageNum, pageSize);

        Page<CommentStatVO> page = new Page<>(pageNum, pageSize);

        // 查询留言
        Page<CommentStatVO> commentPage = bizCommentDao.selectCommentList(page, itemId);

        // 返回结果
        return new PageResult<>(commentPage.getRecords(), commentPage.getTotal(), pageNum, pageSize);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId){
        Long userId=BaseContext.getCurrentId();
        log.info("删除留言开始，userId={}, commentId={}", userId, commentId);

        BizComment comment=bizCommentDao.selectById(commentId);
        if(comment==null){
            log.warn("删除留言失败，留言不存在，commentId={}, userId={}", commentId, userId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        //判断留言拥有者或管理员是否进行删除
        if(!comment.getUserId().equals(userId)&&!BaseContext.getCurrentRole().equals("admin")){
            log.warn("删除留言失败，非留言拥有者或管理员，commentId={}, userId={}", commentId, userId);
            throw new DeletionNotAllowedException(MessageConstant.DELETE_NOT_ALLOWED);
        }

        bizCommentDao.deleteById(commentId);
        log.info("删除留言成功，commentId={}, userId={}", commentId, userId);
    }

    @Override
    public CommentDetailVO getCommentDetail(Long commentId) {
        log.info("查询留言详情，commentId={}", commentId);

        // 查询留言
        BizComment comment = bizCommentDao.selectById(commentId);
        if (comment == null) {
            log.warn("留言不存在，commentId={}", commentId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        // 获取留言的用户信息
        SysUser user = UserDao.selectById(comment.getUserId());
        if (user == null) {
            log.warn("用户信息不存在，userId={}", comment.getUserId());
            throw new AbsentException(MessageConstant.USER_NOT_FOUND);
        }

        // 封装到 VO 中
        CommentDetailVO vo = new CommentDetailVO();
        vo.setId(comment.getId());
        vo.setItemId(comment.getItemId());
        vo.setUserId(comment.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setCreateTime(comment.getCreateTime());

        log.info("查询留言详情成功，commentId={}", commentId);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("将留言标记为已读，commentId={}, userId={}", commentId, userId);

        BizComment comment = bizCommentDao.selectById(commentId);
        if (comment == null) {
            log.warn("留言不存在，commentId={}", commentId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        // 只允许修改未读留言
        if (comment.getIsRead() == 1) {
            log.warn("留言已经是已读状态，commentId={}", commentId);
            return;
        }

        BizComment updateComment = new BizComment();
        updateComment.setId(commentId);
        updateComment.setIsRead(1); // 更新为已读
        bizCommentDao.updateById(updateComment);

        log.info("留言标记为已读成功，commentId={}", commentId);
    }


}
