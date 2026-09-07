package com.app.rbac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.rbac.entity.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long>{
	
	List<RolePermission> findByRoleId(Long roleId);
	

	boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
	
}
