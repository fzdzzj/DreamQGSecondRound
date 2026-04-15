package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
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
 * 提供私聊相关的接口，如发送私聊消息、获取聊天记录、将会话全部标记为已读等
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
     *
     * @param sendDTO 发送私聊消息的参数
     * @return 成功结果
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
     * 获取聊天记录
     *
     * @param peerId        对方用户ID
     * @param lastMessageId 最后一条消息ID
     * @param pageSize      每页数量
     * @return 聊天记录列表
     */
    @GetMapping("/history/cursor/{peerId}")
    @Operation(summary = "获取聊天记录")
    public Result<List<PrivateMessageVO>> getChatHistoryByCursor(
            @PathVariable Long peerId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取聊天记录，peerId={}, lastMessageId={}, pageSize={}", peerId, lastMessageId, pageSize);
        List<PrivateMessageVO> list = privateMessageService.getHistoryByCursor(peerId, lastMessageId, pageSize);
        log.info("获取聊天记录成功，peerId={}, lastMessageId={}, pageSize={}, total={}",
                peerId, lastMessageId, pageSize, list.size());
        return Result.success(list);
    }


    /**
     * 将与某人的会话全部标记为已读
     *
     * @param peerId 对方用户ID
     * @return 成功结果
     */
    @PutMapping("/{peerId}/read")
    @Operation(summary = "将会话全部标记为已读")
    public Result<Void> markConversationAsRead(@PathVariable Long peerId) {
        Long userId = BaseContext.getCurrentId();
        log.info("将会话全部标记为已读，userId={}, peerId={}", userId, peerId);

        privateMessageService.markConversationRead(peerId);

        log.info("将会话全部标记为已读成功，userId={}, peerId={}", userId, peerId);
        return Result.success();
    }

    /**
     * 获取会话列表
     *
     * @return 会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<ConversationVO>> getConversationList() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取会话列表，userId={}", userId);
        List<ConversationVO> list = privateMessageService.getConversations();
        log.info("获取会话列表成功，userId={}, total={}", userId, list.size());
        return Result.success(list);
    }


    /**
     * 删除单条消息
     *
     * @param id 消息ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除单条消息")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除单条消息，userId={}, id={}", userId, id);

        privateMessageService.deleteMessage(id);

        log.info("删除单条消息成功，userId={}, id={}", userId, id);
        return Result.success();
    }

    /**
     * 删除会话
     *
     * @param peerId 对方用户ID
     * @return 成功结果
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
     *
     * @return 未读消息总数
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

    /**
     * 清空会话
     *
     * @param peerId 对方用户ID
     * @return 成功结果
     */
    @PutMapping("/conversation/{peerId}/clear")
    @Operation(summary = "清空会话")
    public Result<Void> clearConversation(@PathVariable Long peerId) {
        Long userId = BaseContext.getCurrentId();
        log.info("清空会话，userId={}, peerId={}", userId, peerId);
        privateMessageService.clearConversation(peerId);
        log.info("清空会话成功，userId={}, peerId={}", userId, peerId);
        return Result.success();
    }
}
