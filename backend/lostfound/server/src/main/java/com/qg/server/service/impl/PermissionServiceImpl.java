package com.qg.server.service.impl;

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
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionDao permissionDao;

    /**
     * 根据角色查询权限
     *
     * @param role 角色
     * @return 权限
     */
    @Override
    public Set<String> getPermissionsByRole(String role) {
        log.info("查询角色{}的权限", role);
        // 直接调用 DAO 查询数据库
        List<Permission> permissions = permissionDao.selectPermissionsByRole(role);
        log.info("角色{}的权限: size: {}", role, permissions.size());
        // 转化为 "METHOD:URL" 格式返回
        return permissions.stream()
                .map(p -> p.getMethod() + ":" + p.getUrl())
                .collect(Collectors.toSet());
    }
}
