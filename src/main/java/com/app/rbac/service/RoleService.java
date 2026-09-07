package com.app.rbac.service;

import java.util.List;

import com.app.rbac.dto.RoleRequest;
import com.app.rbac.dto.RoleResponse;
import com.app.rbac.entity.Role;


public interface RoleService {

	RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getAllRoles();
    Role getRoleEntityOrThrow(Long roleId);
}
