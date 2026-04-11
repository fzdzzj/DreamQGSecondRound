package com.qg.server.service.impl;

import com.qg.common.constant.Role;
import com.qg.server.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 角色权限服务
 *
 * 当前阶段先使用代码内置权限，保证中期前 RBAC 能稳定跑通。
 * 后续如果补了权限表，再切换为数据库驱动即可。
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Override
    public Set<String> getPermissionsByRole(String role) {

        // 管理员：默认拥有更高权限，后续可用户管理、统计用户管理、统计分析等接口
        if (Role.ADMIN.equals(role) || Role.SYSTEM.equals(role)) {
            return Set.of(
                    "PUT:/admin/user/{id}/disable",
                    "PUT:/admin/user/{id}/enable",

                    "GET:/common/personal-info",
                    "PUT:/common/personal-info",
                    "PUT:/common/password",

                    "POST:/item/lost",
                    "POST:/item/found",
                    "PUT:/item/{id}",
                    "DELETE:/item/{id}",
                    "POST:/item/page",
                    "GET:/item/{id}",
                    "PUT:/item/{id}/close",

                    "POST:/api/file/upload",

                    "POST:/auth/logout",
                    "POST:/auth/refresh",

                    // 预留管理员接口权限
                    "POST:/report",
                    "POST:/report/audit",
                    "POST:/report/list",
                    "GET:/report/{id}",

                    "POST:/pin/page",
                    "POST:/pin/audit",
                    "POST:/pin/cancel/{id}",
                    "GET:/pin/{id}"
            );
        }

        // 普通用户（学生）权限
        return Set.of(
                // 刷新、登出接口
                "POST:/auth/logout",
                "POST:/auth/refresh",
                // 个人信息接口
                "GET:/common/personal-info",
                "PUT:/common/personal-info",
                "PUT:/common/password",


                // 物品接口接口
                "POST:/item/lost",
                "POST:/item/found",
                "PUT:/item/{id}",
                "DELETE:/item/{id}",
                "POST:/item/page",
                "POST:/item/my/page",
                "PUT:/item/{id}/close",
                "GET:/item/{id}",


                "PUT:/item/pin/apply",

                "POST:/api/file/upload",



                "POST:/pin/apply",


                "POST:/pin/cancel/{id}",
                "GET:/pin/{id}",
                "GET:/pin/mylist",

                "POST:/report",
                "GET:/report/{id}"
        );
    }
}
