package com.qg.server.controller.ai;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.server.service.AiAsyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI接口", description = "AI接口")
@Slf4j
@RequiredArgsConstructor
public class AIController {
    private final AiAsyncService aiAsyncService;

    @PostMapping("/item/{itemId}/regenerate")
    @Operation(summary="重新生成物品AI描述")
    public Result<Void> regenerateItemDescription(@PathVariable Long itemId)
    {
        Long userId = BaseContext.getCurrentId();
        log.info("重新生成物品AI描述，用户ID={},物品ID={}", userId, itemId);
        aiAsyncService.regenerateItemDescription(userId, itemId);
        log.info("重新生成物品AI描述成功，用户ID={},物品ID={}", userId, itemId);
        return Result.success();
    }

}
