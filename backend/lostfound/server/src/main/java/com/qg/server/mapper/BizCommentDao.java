package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.vo.CommentStatVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizCommentDao extends BaseMapper<BizComment> {

    Page<CommentStatVO> selectCommentList(Page<CommentStatVO> page, @Param("itemId") Long itemId);

    List<CommentStatVO> selectCommentsByParentIds(@Param("itemId") Long itemId,
                                                  @Param("parentIds") List<Long> parentIds);
}
