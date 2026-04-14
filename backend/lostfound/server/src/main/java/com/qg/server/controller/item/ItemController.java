package com.qg.server.controller.item;

import com.qg.common.constant.LimitTypeConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.dto.ItemPageQueryDTO;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.pojo.vo.BizItemDetailVO;
import com.qg.server.anno.AntiBot;
import com.qg.server.service.ItemService;
import com.qg.server.service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "物品接口")
public class ItemController {

    private final ItemService itemService;
    private final PinService pinService;

    @PostMapping("/lost")
    @Operation(summary = "发布丢失物品")
    @AntiBot(value = LimitTypeConstant.USER_POST_LIMIT)
    public Result<Void> publishLostItem(@Validated @RequestBody LostBizItemDTO lostBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布丢失物品，用户ID={}", userId);
        itemService.publishLostItem(lostBizItemDTO);
        log.info("发布丢失物品成功，用户ID={}", userId);
        return Result.success();
    }

    @PostMapping("/found")
    @Operation(summary = "发布拾取物品")
    @AntiBot(value = LimitTypeConstant.USER_POST_LIMIT)
    public Result<Void> publishFoundItem(@Validated @RequestBody LostBizItemDTO lostBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布拾取物品，用户ID={}", userId);
        itemService.publishFoundItem(lostBizItemDTO);
        log.info("发布拾取物品成功，用户ID={}", userId);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改物品信息")
    @AntiBot(value = LimitTypeConstant.USER_EDIT_POST_LIMIT)
    public Result<Void> updateItem(@PathVariable Long id,
                                   @Validated @RequestBody UpdateBizItemDTO updateBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改物品信息，物品ID={}, 用户ID={}", id, userId);
        itemService.updateItem(id, updateBizItemDTO);
        log.info("修改物品信息成功，物品ID={}, 用户ID={}", id, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取物品详情")
    public Result<BizItemDetailVO> getItem(@PathVariable Long id) {
        log.info("获取物品详情，物品ID={}", id);
        BizItemDetailVO bizItemDetailVO = itemService.getItem(id);
        log.info("获取物品详情成功，物品ID={}", id);
        return Result.success(bizItemDetailVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物品")
    public Result<Void> deleteItem(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除物品，物品ID={}, 用户ID={}", id, userId);
        itemService.deleteItem(id);
        log.info("删除物品成功，物品ID={}, 用户ID={}", id, userId);
        return Result.success();
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询列表")
    public Result<PageResult<BizItemStatVO>> page(@Validated @RequestBody ItemPageQueryDTO query) {
        log.info("分页查询列表，请求参数={}", query);
        PageResult<BizItemStatVO> pageResult = itemService.pageList(query);
        log.info("分页查询列表成功");
        return Result.success(pageResult);
    }

    @PostMapping("/my/page")
    @Operation(summary = "分页查询我的物品")
    public Result<PageResult<BizItemStatVO>> myPage(@Validated @RequestBody ItemPageQueryDTO query) {
        Long userId = BaseContext.getCurrentId();
        log.info("分页查询我的物品，userId={}, query={}", userId, query);
        PageResult<BizItemStatVO> pageResult = itemService.myPageList(query);
        log.info("分页查询我的物品成功，userId={}", userId);
        return Result.success(pageResult);
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭物品")
    public Result<Void> closeItem(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("关闭物品，itemId={}, userId={}", id, userId);
        itemService.closeItem(id);
        log.info("关闭物品成功，itemId={}, userId={}", id, userId);
        return Result.success();
    }

    @PutMapping("/pin/apply")
    @Operation(summary = "申请置顶")
    public Result<Void> applyPin(@Validated @RequestBody PinApplyDTO pinApplyDTO){
        Long userId=BaseContext.getCurrentId();
        log.info("申请置顶，itemId={}, userId={}", pinApplyDTO.getItemId(), userId);
        pinService.apply(pinApplyDTO);
        log.info("申请置顶成功，itemId={}, userId={}", pinApplyDTO.getItemId(), userId);
        return Result.success();
    }



}
