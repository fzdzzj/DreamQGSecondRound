package com.qg.server.controller;

import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.UserPageQueryDTO;
import com.qg.pojo.vo.SysUserDetailVO;
import com.qg.pojo.vo.SysUserStatVO;
import com.qg.server.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员接口")
public class AdminController {
    private final AdminService adminService;
    /**
     * 用户分页列表
     */
    @PostMapping("/user/list")
    @Operation(summary = "用户分页列表")
    public Result<PageResult<SysUserStatVO>> userList(@Validated @RequestBody UserPageQueryDTO queryDTO) {
        log.info("管理员获取用户列表，pageNum={}, pageSize={}", queryDTO.getPageNum(), queryDTO.getPageSize());
        PageResult<SysUserStatVO> pageResult = adminService.userList(queryDTO);
        log.info("用户列表获取成功，total={}, pageNum={}, pageSize={}",
                pageResult.getTotal(), pageResult.getPageNum(), pageResult.getPageSize());
        return Result.success(pageResult);
    }
}
