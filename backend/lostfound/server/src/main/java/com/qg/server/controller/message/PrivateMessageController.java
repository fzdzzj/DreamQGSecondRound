package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;
import com.qg.server.service.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私聊接口
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "私聊接口")
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    /**
     * 发送私聊消息
     */
    @PostMapping
    @Operation(summary = "发送私聊消息")
    public Result<Void> sendMessage(@Valid @RequestBody PrivateMessageSendDTO sendDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("发送私聊消息，senderId={}, receiverId={}", userId, sendDTO.getReceiverId());

        privateMessageService.sendMessage(sendDTO);

        log.info("发送私聊消息成功，senderId={}, receiverId={}", userId, sendDTO.getReceiverId());
        return Result.success();
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<ConversationVO>> getConversationList() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取会话列表，userId={}", userId);

        List<ConversationVO> list = privateMessageService.getConversationList();

        log.info("获取会话列表成功，userId={}, size={}", userId, list.size());
        return Result.success(list);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/history/{peerId}")
    @Operation(summary = "获取聊天记录")
    public Result<PageResult<PrivateMessageVO>> getChatHistory(@PathVariable Long peerId,
                                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取聊天记录，userId={}, peerId={}, pageNum={}, pageSize={}",
                userId, peerId, pageNum, pageSize);

        PageResult<PrivateMessageVO> pageResult = privateMessageService.getChatHistory(peerId, pageNum, pageSize);

        log.info("获取聊天记录成功，userId={}, peerId={}, total={}",
                userId, peerId, pageResult.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/{messageId}/read")
    @Operation(summary = "标记单条消息为已读")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        Long userId = BaseContext.getCurrentId();
        log.info("标记单条消息为已读，userId={}, messageId={}", userId, messageId);

        privateMessageService.markAsRead(messageId);

        log.info("标记单条消息为已读成功，userId={}, messageId={}", userId, messageId);
        return Result.success();
    }

    /**
     * 将与某人的会话全部标记为已读
     */
    @PutMapping("/conversation/{peerId}/read")
    @Operation(summary = "将会话全部标记为已读")
    public Result<Void> markConversationAsRead(@PathVariable Long peerId) {
        Long userId = BaseContext.getCurrentId();
        log.info("将会话全部标记为已读，userId={}, peerId={}", userId, peerId);

        privateMessageService.markConversationAsRead(peerId);

        log.info("将会话全部标记为已读成功，userId={}, peerId={}", userId, peerId);
        return Result.success();
    }

    /**
     * 删除单条消息
     */
    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除单条消息")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除单条消息，userId={}, messageId={}", userId, messageId);

        privateMessageService.deleteMessage(messageId);

        log.info("删除单条消息成功，userId={}, messageId={}", userId, messageId);
        return Result.success();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversation/{peerId}")
    @Operation(summary = "删除会话")
    public Result<Void> deleteConversation(@PathVariable Long peerId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除会话，userId={}, peerId={}", userId, peerId);

        privateMessageService.deleteConversation(peerId);

        log.info("删除会话成功，userId={}, peerId={}", userId, peerId);
        return Result.success();
    }

    /**
     * 获取未读消息总数
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息总数")
    public Result<Long> getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取未读消息总数，userId={}", userId);

        Long count = privateMessageService.getUnreadCount();

        log.info("获取未读消息总数成功，userId={}, count={}", userId, count);
        return Result.success(count);
    }
}
