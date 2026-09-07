package com.app.rbac.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.rbac.dto.PermissionRequest;
import com.app.rbac.dto.PermissionResponse;
import com.app.rbac.entity.Permission;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.exception.ResourceNotFoundException;
import com.app.rbac.repository.PermissionRepository;
import com.app.rbac.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService{
	
	private final PermissionRepository permissionRepository;
	
	@Override
	public PermissionResponse createPermission(PermissionRequest permissionRequest) {
		// TODO Auto-generated method stub
		if(permissionRepository.existsByName(permissionRequest.getName())) {
			throw new DuplicateResourceException("Permission Alredy exists: "+ permissionRequest.getName());
	}
		Permission saved = permissionRepository.save(Permission.builder().name(permissionRequest.getName()).build());
		return toResponse(saved);
		
	}

	@Override
	public List<PermissionResponse> getAllPermission() {
		// TODO Auto-generated method stub
		return permissionRepository.findAll().stream()
				.map(this:: toResponse).toList();
	}

	@Override
	public Permission getPermissionEntityOrThrow(Long permissionId) {
		// TODO Auto-generated method stub
		return permissionRepository.findById(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("permission not found with id: "+permissionId));
	}
	
	private PermissionResponse toResponse(Permission permission) {
		// TODO Auto-generated method stub
		return new PermissionResponse(permission.getId(),permission.getName());
	}

}
