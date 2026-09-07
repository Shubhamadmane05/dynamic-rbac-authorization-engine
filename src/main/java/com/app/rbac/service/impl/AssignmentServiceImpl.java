package com.app.rbac.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.app.rbac.dto.ApiMessageResponse;
import com.app.rbac.entity.AppUser;
import com.app.rbac.entity.Permission;
import com.app.rbac.entity.Role;
import com.app.rbac.entity.RolePermission;
import com.app.rbac.entity.UserRole;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.repository.RolePermissionRepository;
import com.app.rbac.repository.UserRoleRepository;
import com.app.rbac.service.AppUserService;
import com.app.rbac.service.AssignmentService;
import com.app.rbac.service.PermissionService;
import com.app.rbac.service.RoleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService{

	private final RoleService roleService;
	private final PermissionService permissionService;
	
	private  final AppUserService appUserService;
	private final RolePermissionRepository rolePermissionRepository;
	private final UserRoleRepository userRoleRepository;
	
	@Override
	@Transactional
	@CacheEvict(value = "userPermissions", allEntries = true)
	public ApiMessageResponse assignPermissionToRole(Long roleId, Long permissionId) {
		// TODO Auto-generated method stub
		Role role = roleService.getRoleEntityOrThrow(roleId);
		Permission permission = permissionService.getPermissionEntityOrThrow(permissionId);
	 	
		
		if(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
			throw new DuplicateResourceException("Permission '" + permission.getName()+ "' is alredy assigned to role '"+ role.getName()+"'");
		}
		
		 rolePermissionRepository.save(RolePermission.builder().role(role).permission(permission).build());
		return new ApiMessageResponse(
				"Permission '"+ permission.getName() + "' assign to role '" + role.getName()+ "'"
				);
	}
	
	@Override
	@Transactional
	@CacheEvict(value = "userPermissions", allEntries = true)
	public ApiMessageResponse assignRoleToUser(Long userId, Long roleId) {
		// TODO Auto-generated method stub
		
		AppUser user = appUserService.getUserEntityOrThrow(userId);
		Role role = roleService.getRoleEntityOrThrow(roleId);
		
		if(userRoleRepository.existsByUserIdAndRoleId(userId, roleId))
			throw new DuplicateResourceException("Role '"+
		role.getName()+ "' is Alredy assigned to user '" + user.getUsername()+"'");
		
		userRoleRepository.save(UserRole.builder().user(user).role(role).build());
		
		return new ApiMessageResponse(
				"Role '" + role.getName()+"' assigned to user '"+user.getUsername() 
				);
	}
	
}
