package com.qg.server.service;

import java.util.Set;

public interface PermissionService {
    Set<String> getPermissionsByRole(String role);
}
