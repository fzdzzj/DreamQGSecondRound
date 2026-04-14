package com.qg.server.service.impl;

import com.qg.common.constant.RoleConstant;
import com.qg.pojo.entity.Permission;
import com.qg.server.mapper.PermissionDao;
import com.qg.server.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色权限服务
 *
 * 当前阶段先使用代码内置权限，保证中期前 RBAC 能稳定跑通。
 * 后续如果补了权限表，再切换为数据库驱动即可。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionDao permissionDao;
    @Override
    public Set<String> getPermissionsByRole(String role) {

        // 直接调用 DAO 查询数据库
        List<Permission> permissions = permissionDao.selectPermissionsByRoleCode(role);

        // 转化为 "METHOD:URL" 格式返回
        return permissions.stream()
                .map(p -> p.getMethod() + ":" + p.getUrl())
                .collect(Collectors.toSet());
    }
}
