package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.vo.CommentStatVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物品评论数据访问层
 */
public interface BizCommentDao extends BaseMapper<BizComment> {
    /**
     * 分页查询物品评论
     *
     * @param page   分页参数
     * @param itemId 物品ID
     * @return 分页结果
     */
    Page<CommentStatVO> selectCommentList(Page<CommentStatVO> page, @Param("itemId") Long itemId);

    /**
     * 根据父级评论ID查询子级评论
     *
     * @param itemId    物品ID
     * @param parentIds 父级评论ID列表
     * @return 子级评论列表
     */
    List<CommentStatVO> selectCommentsByParentIds(@Param("itemId") Long itemId,
                                                  @Param("parentIds") List<Long> parentIds);
}
