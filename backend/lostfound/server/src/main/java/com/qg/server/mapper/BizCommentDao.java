package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.vo.CommentStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizCommentDao extends BaseMapper<BizComment> {
    /**
     * 查询物品一级评论(分页)
     *
     * @param page   分页信息
     * @param itemId 物品ID
     * @return 留言列表
     */
    Page<CommentStatVO> selectCommentList(Page<CommentStatVO> page, @Param("itemId") Long itemId);

    /**
     * 查询物品所有子评论
     */
    List<CommentStatVO> selectChildCommentList(@Param("itemId") Long itemId);
}