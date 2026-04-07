package com.qg.server.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限数据访问接口
 */
@Mapper
public interface PermissionDao {
    List<String> selectPermissionsByRoleCode(String roleCode);
}
