package com.qg.server.controller.user;

import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.AdminStatisticsQueryDTO;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.vo.AdminStatisticsVO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;
import com.qg.server.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员接口
 * 提供管理员相关的接口，如用户分页列表、获取用户详情、封禁用户、解封用户等
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员接口")
public class AdminController {
    private final AdminService adminService;

    /**
     * 用户分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping("/user")
    @Operation(summary = "用户分页列表")
    public Result<PageResult<SysUserStatVO>> userList(@Validated UserPageQueryDTO queryDTO) {
        log.info("管理员获取用户列表，pageNum={}, pageSize={}", queryDTO.getPageNum(), queryDTO.getPageSize());
        PageResult<SysUserStatVO> pageResult = adminService.userList(queryDTO);
        log.info("用户列表获取成功，total={}, pageNum={}, pageSize={}",
                pageResult.getTotal(), pageResult.getPageNum(), pageResult.getPageSize());
        return Result.success(pageResult);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/user/{id}")
    @Operation(summary = "获取用户详情")
    public Result<SysUserDetailVO> getUserDetail(@PathVariable Long id) {
        log.info("管理员获取用户详情，userId={}", id);
        SysUserDetailVO vo = adminService.userDetail(id);
        log.info("用户详情获取成功，userId={}", id);
        return Result.success(vo);
    }

    /**
     * 封禁用户
     *
     * @param id 用户ID
     * @return 封禁成功结果
     */
    @PutMapping("/user/{id}/disable")
    @Operation(summary = "封禁用户")
    public Result<Void> disableUser(@PathVariable Long id) {
        log.info("管理员请求封禁用户，userId={}", id);
        adminService.disableUser(id);
        log.info("管理员请求封禁用户成功，userId={}", id);
        return Result.success();
    }

    /**
     * 解封用户
     *
     * @param id 用户ID
     * @return 解封成功结果
     */
    @PutMapping("/user/{id}/enable")
    @Operation(summary = "解封用户")
    public Result<Void> enableUser(@PathVariable Long id) {
        log.info("管理员请求解封用户，userId={}", id);
        adminService.enableUser(id);
        log.info("管理员请求解封用户成功，userId={}", id);
        return Result.success();
    }

    /**
     * 管理员删除物品
     *
     * @param id 物品ID
     * @return 删除成功结果
     */
    @DeleteMapping("/item/{id}")
    @Operation(summary = "管理员删除物品")
    public Result<Void> deleteItem(@PathVariable Long id) {
        log.info("管理员请求删除物品，itemId={}", id);
        adminService.deleteItem(id);
        log.info("管理员请求删除物品成功，itemId={}", id);
        return Result.success();
    }

    /**
     * 平台统计
     *
     * @param dto 查询条件
     * @return 统计结果
     */
    @GetMapping("/statistics")
    @Operation(summary = "平台统计")
    public Result<AdminStatisticsVO> statistics(@RequestBody(required = false) AdminStatisticsQueryDTO dto) {
        log.info("管理员请求平台统计，startTime={}, endTime={}", dto.getStartTime(), dto.getEndTime());
        AdminStatisticsVO vo = adminService.statistics(dto);
        log.info("管理员请求平台统计成功,size={}", vo.getFoundCount());
        return Result.success(vo);
    }


}
