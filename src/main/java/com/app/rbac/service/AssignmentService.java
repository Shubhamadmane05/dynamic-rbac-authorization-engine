package com.app.rbac.service;

import com.app.rbac.dto.ApiMessageResponse;

public interface AssignmentService {

	ApiMessageResponse assignPermissionToRole(Long roleId, Long permissionId);
	ApiMessageResponse assignRoleToUser(Long userId, Long roleId);
}
