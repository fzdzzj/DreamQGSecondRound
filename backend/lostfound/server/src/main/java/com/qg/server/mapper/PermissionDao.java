package com.qg.server.mapper;

import com.qg.pojo.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限数据访问接口
 */
@Mapper
public interface PermissionDao {
    /**
     * 根据角色编号查询权限
     */
    List<Permission> selectPermissionsByRole(String role);
}
