package com.qg.server.service;

import java.util.Set;
/**
 * 权限服务
 */
public interface PermissionService {
    /**
     * 根据角色获取权限
     * @param role 角色
     * @return 权限集合
     */
    Set<String> getPermissionsByRole(String role);
}
