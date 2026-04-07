package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.query.ItemPageQuery;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.pojo.vo.BizItemVO;

public interface ItemService {
    void publishLostItem(LostBizItemDTO lostBizItemDTO);

    void publishFoundItem(LostBizItemDTO lostBizItemDTO);

    void updateItem(Long id, UpdateBizItemDTO updateBizItemDTO);

    BizItemVO getItem(Long id);

    void deleteItem(Long id);

    PageResult<BizItemStatVO> pageList(ItemPageQuery query);

    PageResult<BizItemStatVO> myPageList(ItemPageQuery query);

    void closeItem(Long id);

}
