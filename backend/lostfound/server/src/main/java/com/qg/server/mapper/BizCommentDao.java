package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.pojo.entity.BizComment;
import com.qg.pojo.vo.CommentStatVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizCommentDao extends BaseMapper<BizComment> {
    /**
     * 根据物品ID获取留言列表
     *
     * @param page   分页信息
     * @param itemId 物品ID
     * @return 留言列表
     */
    Page<CommentStatVO> selectCommentList(Page<CommentStatVO> page, Long itemId);
}