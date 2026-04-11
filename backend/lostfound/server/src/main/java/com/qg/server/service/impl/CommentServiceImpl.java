package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.ReadStatus;
import com.qg.common.constant.Role;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.ReadStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.exception.UpdateNotAllowedException;
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
import com.qg.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<BizCommentDao, BizComment> implements CommentService {

    private final BizItemDao bizItemDao;  // 物品数据访问层
    private final UserDao userDao;  // 用户数据访问层
    private final NotificationService notificationService;  // 通知服务层
    private final BizCommentDao bizCommentDao;

    /**
     * 新增留言
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentAddDTO commentAddDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增留言开始，userId={}, itemId={}", userId, commentAddDTO.getItemId());

        // 校验物品是否存在
        BizItem bizItem = bizItemDao.selectById(commentAddDTO.getItemId());
        if (bizItem == null) {
            log.warn("新增留言失败，物品不存在，itemId={}, userId={}", commentAddDTO.getItemId(), userId);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 创建新的留言
        BizComment bizComment = new BizComment();
        bizComment.setItemId(commentAddDTO.getItemId());
        bizComment.setUserId(userId);
        bizComment.setContent(commentAddDTO.getContent());
        if(commentAddDTO.getParentId() !=0){
            BizComment parentComment =getById(commentAddDTO.getParentId());
            if(parentComment == null){
                log.warn("新增留言失败，父留言不存在，parentId={}, userId={}", commentAddDTO.getParentId(), userId);
                throw new AbsentException(MessageConstant.PARENT_COMMENT_NOT_FOUND);
            }

        }
        bizComment.setParentId(commentAddDTO.getParentId() == null ? 0L : commentAddDTO.getParentId());
        bizComment.setIsRead(ReadStatus.UNREAD);

        // 保存留言
        save(bizComment); // 使用 IService 提供的 save 方法

        log.info("新增留言成功，commentId={}, userId={}, itemId={}",
                bizComment.getId(), userId, commentAddDTO.getItemId());
    }

    /**
     * 获取物品留言列表（分页）
     */
    @Override
    public PageResult<CommentStatVO> getCommentList(Long itemId, Integer pageNum, Integer pageSize) {
        log.info("查询物品留言，itemId={}, pageNum={}, pageSize={}", itemId, pageNum, pageSize);

        Page<CommentStatVO> page = new Page<>(pageNum, pageSize);

        // 查询留言
        Page<CommentStatVO> commentPage = baseMapper.selectCommentList(page, itemId); // 使用 baseMapper 来替代 bizCommentDao

        // 返回结果
        return new PageResult<>(commentPage.getRecords(), commentPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 删除留言（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除留言开始，userId={}, commentId={}", userId, commentId);

        BizComment comment = getById(commentId);  // 使用 IService 提供的 getById 方法
        if (comment == null) {
            log.warn("删除留言失败，留言不存在，commentId={}, userId={}", commentId, userId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        // 判断留言是否是留言拥有者或管理员进行删除
        if (!comment.getUserId().equals(userId) && !BaseContext.getCurrentRole().equals(Role.ADMIN)) {
            log.warn("删除留言失败，非留言拥有者或管理员，commentId={}, userId={}", commentId, userId);
            throw new DeletionNotAllowedException(MessageConstant.DELETE_NOT_ALLOWED);
        }

        // 删除留言
        removeById(commentId);  // 使用 IService 提供的 removeById 方法
        log.info("删除留言成功，commentId={}, userId={}", commentId, userId);
    }

    /**
     * 获取留言详情
     */
    @Override
    public CommentDetailVO getCommentDetail(Long commentId) {
        log.info("查询留言详情，commentId={}", commentId);

        // 查询留言
        BizComment comment = getById(commentId);  // 使用 IService 提供的 getById 方法
        if (comment == null) {
            log.warn("留言不存在，commentId={}", commentId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        // 获取留言的用户信息
        SysUser user = userDao.selectById(comment.getUserId());
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
        vo.setIsRead(comment.getIsRead());
        vo.setDeleted(comment.getDeleted());
        vo.setUpdateTime(comment.getUpdateTime());

        log.info("查询留言详情成功，commentId={}", commentId);
        return vo;
    }

    /**
     * 标记留言为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("将留言标记为已读，commentId={}, userId={}", commentId, userId);

        BizComment comment = getById(commentId);  // 使用 IService 提供的 getById 方法
        if (comment == null) {
            log.warn("留言不存在，commentId={}", commentId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }
        Long itemId = comment.getItemId();
        if (!bizItemDao.selectById(itemId).getUserId().equals(userId)) {
            log.warn("非物品拥有者，无法将留言标记为已读，commentId={}, userId={}", commentId, userId);
            throw new AbsentException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        // 标记为已读
        BizComment updateComment = new BizComment();
        updateComment.setId(commentId);
        updateComment.setIsRead(ReadStatus.READ);  // 设置为已读
        updateById(updateComment);  // 使用 IService 提供的 updateById 方法
        log.info("留言标记为已读成功，commentId={}", commentId);
    }

    /**
     * 获取物品下未读留言数量
     */
    @Override
    public Long getUnreadCount(Long itemId) {
        log.info("查询物品下未读留言数量，itemId={}", itemId);
        Long userId = BaseContext.getCurrentId();
        if (!bizItemDao.selectById(itemId).getUserId().equals(userId)) {
            log.warn("非物品拥有者，无法获取物品下未读留言数量，itemId={}, userId={}", itemId, userId);
            throw new AbsentException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        Long count = count(
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getItemId, itemId)
                        .eq(BizComment::getIsRead, ReadStatus.UNREAD)
        );  // 使用 IService 提供的 count 方法

        log.info("查询物品下未读留言数量成功，itemId={}, count={}", itemId, count);
        return count;
    }

    /**
     * 获取用户未读留言数量
     */
    @Override
    public Long getUserUnreadCount(Long userId) {
        log.info("查询用户未读留言数量，userId={}", userId);
        //先找出用户发布了的物品
        List<BizItem> items = bizItemDao.selectList(
                new LambdaQueryWrapper<BizItem>()
                        .eq(BizItem::getUserId, userId)
        );
        //找出用户未读的留言
        List<BizComment> comments = bizCommentDao.selectList(
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getIsRead, ReadStatus.UNREAD)
                        .in(BizComment::getItemId, items)
        );
        Long count = (long) comments.size();
        log.info("查询用户未读留言数量成功，userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 回复留言
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyComment(CommentAddDTO commentAddDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("回复留言开始，userId={}, itemId={}, parentId={}", userId, commentAddDTO.getItemId(), commentAddDTO.getParentId());

        BizItem bizItem = bizItemDao.selectById(commentAddDTO.getItemId());
        if (bizItem == null) {
            log.warn("回复留言失败，物品不存在，itemId={}", commentAddDTO.getItemId());
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 父留言验证
        if (commentAddDTO.getParentId() != 0) {
            BizComment parentComment = getById(commentAddDTO.getParentId());  // 使用 IService 提供的 getById 方法
            if (parentComment == null) {
                log.warn("回复留言失败，父留言不存在，parentId={}", commentAddDTO.getParentId());
                throw new AbsentException(MessageConstant.PARENT_COMMENT_NOT_FOUND);
            }

            // 创建通知：通知父留言的用户
            Long parentUserId = parentComment.getUserId();
            if (parentUserId.equals(userId)) {
                log.info("回复自己留言不用通知，parentId={}", commentAddDTO.getParentId());
            }else{
                log.info("回复留言通知父留言用户，parentId={}", commentAddDTO.getParentId());
                String content = "您的留言有新的回复：" + commentAddDTO.getContent();
                notificationService.createNotification(parentUserId, commentAddDTO.getParentId(), content); // 创建通知
            }
        }

        // 创建回复留言
        BizComment bizComment = new BizComment();
        bizComment.setItemId(commentAddDTO.getItemId());
        bizComment.setUserId(userId);
        bizComment.setContent(commentAddDTO.getContent());
        bizComment.setParentId(commentAddDTO.getParentId());
        bizComment.setIsRead(ReadStatus.UNREAD);  // 设置为未读

        save(bizComment);  // 使用 IService 提供的 save 方法

        log.info("回复留言成功，commentId={}, userId={}, itemId={}",
                bizComment.getId(), userId, commentAddDTO.getItemId());
    }
}
