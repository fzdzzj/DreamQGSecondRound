package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.constant.BizItemAiResultStatus;
import com.qg.common.constant.BizItemStatus;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.server.ai.prompt.ItemAiPromptBuilder;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAsyncServiceImpl implements AiAsyncService {
    private final BizItemDao bizItemDao;
    private final BizItemAiResultDao bizItemAiResultDao;
    private final ChatClient chatClient;
    private final RedisTemplate<String,Object>redisTemplate;
    private final ItemAiPromptBuilder itemAiPromptBuilder;

    private static final String ITEM_DETAIL_KEY="item:detail:";
    private static final String ITEM_PAGE_KEY="item:page";
    private static final String MODEL="gpt-3.5-turbo";
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateItemDescription(Long itemId, String triggerType) {
        log.info("开始生成物品AI描述，itemId={},triggerType={}",itemId,triggerType);

        BizItem item=bizItemDao.selectById(itemId);
        if(item==null){
            log.warn("AI生成失败，物品不存在，itemId={}",itemId);
            return;
        }
        if(!BizItemStatus.OPEN.equals(item.getStatus())){
            log.warn("AI生成跳过，物品状态不允许分析，itemId={},status={}",itemId,item.getStatus());
            return;
        }
        Integer nextVersion=getNextVersion(itemId);

        BizItemAiResult aiResult=new BizItemAiResult();
        aiResult.setItemId(itemId);
        aiResult.setResultVersion(nextVersion);
        aiResult.setSourceType(triggerType);
        aiResult.setStatus(BizItemAiResultStatus.PENDING);
        aiResult.setIsDeleted(0);

        String prompt=itemAiPromptBuilder.buildItemDescriptionPrompt(item);
        aiResult.setPromptText(prompt);
        aiResult.setOriginText(item.getDescription());

        bizItemAiResultDao.insert(aiResult);

        try{
            // 调用AI生成
            String aiText=chatClient.prompt()
                    .user( prompt)
                    .call()
                    .content();

            String aiDescription=aiText;
            String aiCategory=item.getAiCategory();
            String aiTags=item.getAiTags();

            aiResult.setAiDescription(aiDescription);
            aiResult.setAiCategory(aiCategory);
            aiResult.setAiTags(aiTags);
            aiResult.setModelName(MODEL);
            aiResult.setStatus(BizItemAiResultStatus.SUCCESS);
            bizItemAiResultDao.updateById(aiResult);

            //回写主表摘要
            BizItem updateItem=new BizItem();
            updateItem.setId(itemId);
            updateItem.setCurrentAiResultId(aiResult.getId());
            updateItem.setAiStatus(BizItemAiResultStatus.SUCCESS);
            updateItem.setAiCategory(aiCategory);
            updateItem.setAiTags(aiTags);
            bizItemDao.updateById(updateItem);

            evictItemCaches(itemId);
            log.info("AI生成成功，itemId={}, aiResultId={}", itemId, aiResult.getId());
        }catch (Exception e){
            log.error("AI生成失败，itemId={}",itemId,e);

            aiResult.setStatus(BizItemAiResultStatus.FAILURE);
            aiResult.setErrorMessage(e.getMessage());
            bizItemAiResultDao.updateById(aiResult);

            BizItem failItem = new BizItem();
            failItem.setId(itemId);
            failItem.setAiStatus("FAILED");
            bizItemDao.updateById(failItem);

            evictItemCaches(itemId);
        }
    }

    private Integer getNextVersion(Long itemId){
        BizItemAiResult latest=bizItemAiResultDao.selectOne(new LambdaQueryWrapper<BizItemAiResult>()
                .eq(BizItemAiResult::getItemId,itemId)
                .eq(BizItemAiResult::getIsDeleted,0)
                .orderByDesc(BizItemAiResult::getResultVersion)
                .last("limit 1"));
        return latest==null?1:latest.getResultVersion()+1;
    }

    private void evictItemCaches(Long itemId) {
        redisTemplate.delete(ITEM_DETAIL_KEY + itemId);

        Set<String> keys = redisTemplate.keys(ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

}
