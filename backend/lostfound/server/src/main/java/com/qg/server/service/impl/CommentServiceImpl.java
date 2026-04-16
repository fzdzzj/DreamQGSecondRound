package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.DefaultPageConstant;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 留言服务实现类
 * 实现留言服务接口
 * 包含添加留言、获取物品留言列表等功能
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<BizCommentDao, BizComment> implements CommentService {
    private final BizItemDao bizItemDao;  // 物品数据访问层
    private final UserDao userDao;  // 用户数据访问层
    private final BizCommentDao bizCommentDao;
    private final NotificationDao notificationDao;

    /**
     * 新增留言
     *
     * @param dto 留言添加DTO
     *            包含物品ID、评论内容、父级评论ID等信息
     *            如果父级评论ID为空，则为一级评论
     *            如果父级评论ID不为空，则为二级评论
     *            1. 校验物品是否存在
     *            2. 创建评论
     *            3. 如果是回复，发送通知
     *            4. 一级评论通知物品发布者
     *            5. 二级评论通知父级评论用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentAddDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();

        // 1. 校验物品
        BizItem item = bizItemDao.selectById(dto.getItemId());
        if (item == null || item.getDeleted() == 1) {
            log.warn("物品不存在或已删除，itemId: {}", dto.getItemId());
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 2. 创建评论
        BizComment comment = new BizComment();
        comment.setItemId(dto.getItemId());
        comment.setUserId(currentUserId);
        comment.setContent(dto.getContent());

        Long parentId = dto.getParentId();
        if (parentId == null) {
            log.info("父级评论ID为空，itemId: {}", dto.getItemId());
            parentId = 0L;
        }
        comment.setParentId(parentId);

        comment.setIsRead(0);
        bizCommentDao.insert(comment);

        // 3. 如果是回复，发送通知
        if (parentId != 0) {
            log.info("回复评论，parentId: {}", parentId);
            BizComment parent = bizCommentDao.selectById(parentId);

            if (parent != null && !parent.getUserId().equals(currentUserId)) {
                log.info("二级评论，父级评论用户: {}", parent.getUserId());
                Notification notification = createNotification(parent.getUserId(), "有人回复了你的评论", comment.getId());
                notificationDao.insert(notification);
            }
        }

        // 4. 一级评论通知物品发布者
        if (parentId == 0) {
            log.info("一级评论，itemId: {}", dto.getItemId());
            if (!item.getUserId().equals(currentUserId)) {
                log.info("一级评论，物品发布者ID: {}", item.getUserId());
                Notification notification = createNotification(item.getUserId(), "你的物品有新的留言", comment.getId());
                notificationDao.insert(notification);
            }
        }
    }


    /**
     * 获取物品留言列表（分页）
     *
     * @param itemId   物品ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     * 1. 查询一级评论
     * 2. 查询二级评论
     * 3.查询二级评论
     * 4.添加二级评论到一级评论的children列表
     */
    @Override
    public PageResult<CommentStatVO> getCommentList(Long itemId, Integer pageNum, Integer pageSize) {
        log.info("查询物品留言，itemId={}, pageNum={}, pageSize={}", itemId, pageNum, pageSize);
        if (pageNum == null|| pageNum<=0) {
            pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null|| pageSize<=0) {
            pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
        }
        // 1. 查询一级评论
        Page<CommentStatVO> page = new Page<>(pageNum, pageSize);
        Page<CommentStatVO> commentPage = baseMapper.selectCommentList(page, itemId);
        List<CommentStatVO> rootList = commentPage.getRecords();

        log.info("一级评论数量={}", rootList == null ? 0 : rootList.size());

        if (rootList == null || rootList.isEmpty()) {
            log.info("物品{}暂无留言", itemId);
            return new PageResult<>(rootList, commentPage.getTotal(), pageNum, pageSize);
        }

        Map<Long, CommentStatVO> commentMap = new HashMap<>();
        List<Long> currentParentIds = new ArrayList<>();
        Set<Long> visitedIds = new HashSet<>();

        // 2. 创建评论映射，key为评论ID，value为评论对象
        for (CommentStatVO root : rootList) {
            if (root.getChildren() == null) {
                root.setChildren(new ArrayList<>());
            }
            // 处理删除评论
            normalizeDeletedComment(root);
            commentMap.put(root.getId(), root);
            currentParentIds.add(root.getId());
            visitedIds.add(root.getId());
        }
        log.info("开始查询二级评论，一级评论数量={}", rootList.size());
        List<CommentStatVO> allChildren = new ArrayList<>();
        // 3. 查询二级评论
        while (!currentParentIds.isEmpty()) {
            List<CommentStatVO> nextLevelList = baseMapper.selectCommentsByParentIds(itemId, currentParentIds);

            if (nextLevelList == null || nextLevelList.isEmpty()) {
                break;
            }

            List<Long> nextParentIds = new ArrayList<>();
            // 遍历二级评论
            for (CommentStatVO child : nextLevelList) {
                if (child == null || child.getId() == null) {
                    continue;
                }
                // 避免重复查询
                if (!visitedIds.add(child.getId())) {
                    continue;
                }

                if (child.getChildren() == null) {
                    child.setChildren(new ArrayList<>());
                }
                // 处理删除评论
                normalizeDeletedComment(child);
                // 存储二级评论到映射
                commentMap.put(child.getId(), child);
                allChildren.add(child);
                nextParentIds.add(child.getId());
            }
            // 更新当前父评论ID列表
            currentParentIds = nextParentIds;
        }
        log.info("二级评论数量={}", allChildren == null ? 0 : allChildren.size());
        // 4. 添加二级评论到一级评论的children列表
        for (CommentStatVO child : allChildren) {
            Long parentId = child.getParentId();
            if (parentId == null || parentId.longValue() == 0L) {
                continue;
            }

            CommentStatVO parent = commentMap.get(parentId);
            if (parent != null) {
                parent.getChildren().add(child);
            } else {
                log.warn("发现孤儿评论，childId={}, parentId={}", child.getId(), parentId);
            }
        }
        log.info("添加二级评论到一级评论的children列表完成，一级评论数量={}", rootList.size());
        return new PageResult<>(rootList, commentPage.getTotal(), pageNum, pageSize);
    }



    /**
     * 删除留言（逻辑删除）
     *
     * @param commentId 留言ID
     *                  仅评论创建者或管理员或物品所有者才能删除评论
     *                  1. 判断留言是否存在
     *                  2. 查询物品所有者
     *                  3. 判断留言是否是评论的创建者或管理员进行删除
     *                  4. 删除留言
     *                  5. 通知相关用户评论已被删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除留言开始，userId={}, commentId={}", userId, commentId);
        // 1. 判断留言是否存在
        BizComment comment = getById(commentId);
        if (comment == null) {
            log.warn("删除留言失败，留言不存在，commentId={}, userId={}", commentId, userId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        //2.查询物品所有者
        BizItem item = bizItemDao.selectById(comment.getItemId());
        Long ownerId = item.getUserId();
        log.info("物品所有者ID={}", ownerId);
        // 3.判断留言是否是评论的创建者或管理员进行删除
        if (!comment.getUserId().equals(ownerId) && !BaseContext.getCurrentRole().equals(RoleConstant.ADMIN) && !comment.getUserId().equals(userId)) {
            log.warn("删除留言失败，非评论创建者或管理员或物品所有者，commentId={}, userId={}", commentId, userId);
            throw new DeletionNotAllowedException(MessageConstant.DELETE_NOT_ALLOWED);
        }

        // 4.删除留言
        removeById(commentId);
        //5.通知相关用户评论已被删除
        Notification notification = createNotification(comment.getUserId(), "你的评论已被删除", comment.getId());
        notificationDao.insert(notification);
        log.info("删除留言成功，commentId={}, userId={}", commentId, userId);
    }

    /**
     * 获取留言详情
     *
     * @param commentId 留言ID
     * @return 留言详情VO
     * 1. 查询留言是否存在
     * 2. 查询留言的用户信息是否存在
     * 3. 封装留言详情VO
     */
    @Override
    public CommentDetailVO getCommentDetail(Long commentId) {
        log.info("查询留言详情，commentId={}", commentId);

        // 1.查询留言是否存在
        BizComment comment = getById(commentId);
        if (comment == null) {
            log.warn("留言不存在，commentId={}", commentId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }

        // 2.查询留言的用户信息是否存在
        SysUser user = userDao.selectById(comment.getUserId());
        if (user == null) {
            log.warn("用户信息不存在，userId={}", comment.getUserId());
            throw new AbsentException(MessageConstant.USER_NOT_FOUND);
        }

        // 3.封装留言详情VO
        CommentDetailVO vo = buildCommentDetailVO(comment, user);


        log.info("查询留言详情成功，commentId={}", commentId);
        return vo;
    }

    /**
     * 标记留言为已读
     *
     * @param commentId 留言ID
     *                  仅物品所有者才能将留言标记为已读
     *                  1. 判断留言是否存在
     *                  2. 判断留言是否是评论的创建者或管理员进行标记
     *                  3. 标记留言为已读
     */
    @Override
    public void markAsRead(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("将留言标记为已读，commentId={}, userId={}", commentId, userId);
        // 1. 判断留言是否存在
        BizComment comment = getById(commentId);
        if (comment == null) {
            log.warn("留言不存在，commentId={}", commentId);
            throw new AbsentException(MessageConstant.COMMENT_NOT_FOUND);
        }
        Long itemId = comment.getItemId();
        // 2. 判断留言是否是评论的创建者或管理员进行标记
        if (!bizItemDao.selectById(itemId).getUserId().equals(userId)) {
            log.warn("非物品拥有者，无法将留言标记为已读，commentId={}, userId={}", commentId, userId);
            throw new AbsentException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        // 3. 标记留言为已读
        BizComment updateComment = new BizComment();
        updateComment.setId(commentId);
        updateComment.setIsRead(ReadStatusConstant.READ);  // 设置为已读
        updateById(updateComment);
        log.info("留言标记为已读成功，commentId={}", commentId);
    }

    /**
     * 获取物品下未读留言数量
     * 1. 判断物品是否存在
     * 2. 查询物品下未读留言数量
     * 3. 返回未读留言数量
     */
    @Override
    public Long getUnreadCount(Long itemId) {
        log.info("查询物品下未读留言数量，itemId={}", itemId);
        Long userId = BaseContext.getCurrentId();
        // 1. 判断物品是否存在
        BizItem item = bizItemDao.selectById(itemId);
        log.info("物品信息={}", item);
        if (item == null) {
            log.warn("物品不存在，itemId={}", itemId);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 2. 查询物品下未读留言数量
        if (!item.getUserId().equals(userId)) {
            log.warn("非物品拥有者，无法获取物品下未读留言数量，itemId={}, userId={}", itemId, userId);
            throw new AbsentException("非物品拥有者，无法获取物品下未读留言数量");
        }
        // 3. 返回未读留言数量
        Long count = count(
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getItemId, itemId)
                        .eq(BizComment::getIsRead, ReadStatusConstant.UNREAD)
        );

        log.info("查询物品下未读留言数量成功，itemId={}, count={}", itemId, count);
        return count;
    }

    /**
     * 获取用户未读留言数量
     * 1. 找出用户发布的物品
     * 2. 找出用户未读的留言
     * 3. 返回未读留言数量
     */
    @Override
    public Long getUserUnreadCount(Long userId) {
        log.info("查询用户未读留言数量，userId={}", userId);
        // 1. 找出用户发布了的物品
        List<BizItem> items = bizItemDao.selectList(
                new LambdaQueryWrapper<BizItem>()
                        .eq(BizItem::getUserId, userId)
        );
        if (items.isEmpty()) {
            log.warn("用户没有发布物品，无法查询用户未读留言数量，userId={}", userId);
            return 0L;
        }
        List<Long> itemIds = items.stream().map(BizItem::getId).collect(Collectors.toList());
        log.info("用户发布物品数量={}", itemIds.size());
        // 2. 找出用户未读的留言
        List<BizComment> comments = bizCommentDao.selectList(
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getIsRead, ReadStatusConstant.UNREAD)
                        .in(BizComment::getItemId, itemIds)
        );
        // 3. 返回未读留言数量
        Long count = (long) comments.size();
        log.info("查询用户未读留言数量成功，userId={}, count={}", userId, count);
        return count;
    }
    /**
     * 构建留言详情VO
     *
     * @param comment 留言信息
     * @param user    用户信息
     * @return 留言详情VO
     */
    private CommentDetailVO buildCommentDetailVO(BizComment comment, SysUser user) {
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
        return vo;
    }
    /**
     * 创建通知
     *
     * @param userId    用户ID
     * @param content   通知内容
     * @param commentId 评论ID
     * @return 通知
     */
    private Notification createNotification(Long userId, String content, Long commentId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setCommentId(commentId);
        notification.setIsRead(0);
        return notification;
    }

    /**
     * 处理删除的评论
     *
     * @param comment 评论
     *                如果评论已被删除，将评论内容设置为“该评论已删除”，并设置昵称和头像为空null
     */
    private void normalizeDeletedComment(CommentStatVO comment) {
        if (comment != null && comment.getDeleted() != null && comment.getDeleted() == 1) {
            comment.setContent("该评论已删除");
            comment.setNickname(null);
            comment.setAvatar(null);
        }
    }

}
