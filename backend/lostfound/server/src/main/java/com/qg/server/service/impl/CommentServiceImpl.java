package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.ReadStatusConstant;
import com.qg.common.constant.RoleConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.Notification;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.CommentDetailVO;
import com.qg.pojo.vo.CommentStatVO;
import com.qg.server.mapper.BizCommentDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.NotificationDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.CommentService;
import com.qg.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<BizCommentDao, BizComment> implements CommentService {

    private final BizItemDao bizItemDao;  // 物品数据访问层
    private final UserDao userDao;  // 用户数据访问层
    private final NotificationService notificationService;  // 通知服务层
    private final BizCommentDao bizCommentDao;
    private final NotificationDao notificationDao;

    /**
     * 新增留言
     */
    @Override
    @Transactional
    public void addComment(CommentAddDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();

        // 1. 校验物品
        BizItem item = bizItemDao.selectById(dto.getItemId());
        if (item == null || item.getDeleted() == 1) {
            throw new AbsentException("物品不存在");
        }

        // 2. 创建评论
        BizComment comment = new BizComment();
        comment.setItemId(dto.getItemId());
        comment.setUserId(currentUserId);
        comment.setContent(dto.getContent());

        Long parentId = dto.getParentId();
        if (parentId == null) {
            parentId = 0L;
        }
        comment.setParentId(parentId);

        comment.setIsRead(0);
        bizCommentDao.insert(comment);

        // 3. 如果是回复，发送通知
        if (parentId != 0) {
            BizComment parent = bizCommentDao.selectById(parentId);

            if (parent != null && !parent.getUserId().equals(currentUserId)) {

                Notification notification = new Notification();
                notification.setUserId(parent.getUserId());
                notification.setContent("有人回复了你的评论");
                notification.setCommentId(comment.getId());
                notification.setIsRead(0);

                notificationDao.insert(notification);
            }
        }

        // 4. 一级评论通知物品发布者
        if (parentId == 0) {
            if (!item.getUserId().equals(currentUserId)) {

                Notification notification = new Notification();
                notification.setUserId(item.getUserId());
                notification.setContent("你的物品有新的留言");
                notification.setCommentId(comment.getId());
                notification.setIsRead(0);

                notificationDao.insert(notification);
            }
        }
    }


    /**
     * 获取物品留言列表（分页）
     */
    @Override
    public PageResult<CommentStatVO> getCommentList(Long itemId, Integer pageNum, Integer pageSize) {
        log.info("查询物品留言，itemId={}, pageNum={}, pageSize={}", itemId, pageNum, pageSize);
        //1. 一级评论分页
        Page<CommentStatVO> page = new Page<>(pageNum, pageSize);
        Page<CommentStatVO> commentPage = baseMapper.selectCommentList(page, itemId);
        List<CommentStatVO> rootList=commentPage.getRecords();
        //2.没有一级评论，直接返回
        if(rootList==null||rootList.isEmpty()){
            return new PageResult<>(rootList, commentPage.getTotal(), pageNum, pageSize);
        }

        //2.查询当前物品所有子评论
        List<CommentStatVO> childList=baseMapper.selectChildCommentList(itemId);

        if(childList==null||childList.isEmpty()){
            return new PageResult<>(rootList, commentPage.getTotal(), pageNum, pageSize);
        }

        //3.构建所有评论映射表:id->comment
        Map<Long,CommentStatVO>commentMap=new HashMap<>();

        //先放一级评论
        for(CommentStatVO root:rootList){
            if(root.getChildren()==null){
                root.setChildren(new ArrayList<>());
            }
            commentMap.put(root.getId(),root);
        }

        //再放子评论
        for(CommentStatVO child:childList){
            if(child.getChildren()== null){
                child.setChildren(new ArrayList<>());
            }
            commentMap.put(child.getId(),child);
        }

        //4.组建树
        for(CommentStatVO child:childList){
            Long parentId=child.getParentId();
            if(parentId==null||parentId==0){
                continue;
            }

            CommentStatVO parent=commentMap.get(parentId);
            if(parent!=null){
                parent.getChildren().add(child);
            }
        }

        //5.返回一级评论树
        return new PageResult<>(rootList,commentPage.getTotal(),pageNum,pageSize);

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
        if (!comment.getUserId().equals(userId) && !BaseContext.getCurrentRole().equals(RoleConstant.ADMIN)) {
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
        updateComment.setIsRead(ReadStatusConstant.READ);  // 设置为已读
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
                        .eq(BizComment::getIsRead, ReadStatusConstant.UNREAD)
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
                        .eq(BizComment::getIsRead, ReadStatusConstant.UNREAD)
                        .in(BizComment::getItemId, items)
        );
        Long count = (long) comments.size();
        log.info("查询用户未读留言数量成功，userId={}, count={}", userId, count);
        return count;
    }
}
