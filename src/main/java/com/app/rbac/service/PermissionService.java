package com.app.rbac.service;


import java.util.List;

import com.app.rbac.dto.PermissionRequest;
import com.app.rbac.dto.PermissionResponse;
import com.app.rbac.entity.Permission;

public interface PermissionService {

	PermissionResponse createPermission(PermissionRequest permissionRequest);
	
	List<PermissionResponse>  getAllPermission();
	
Permission getPermissionEntityOrThrow(Long permissionId);
	
}
