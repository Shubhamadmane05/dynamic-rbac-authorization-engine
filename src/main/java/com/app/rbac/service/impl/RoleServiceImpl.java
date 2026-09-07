package com.app.rbac.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.rbac.dto.RoleRequest;
import com.app.rbac.dto.RoleResponse;
import com.app.rbac.entity.Role;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.exception.ResourceNotFoundException;
import com.app.rbac.repository.RoleRepository;
import com.app.rbac.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService{
	
	private final RoleRepository roleRepository;
	
	@Override
	public RoleResponse createRole(RoleRequest request) {
		// TODO Auto-generated method stub
		if(roleRepository.existsByName(request.getName())) {
			throw new DuplicateResourceException("Role Already Exists: "+request.getName());
		}
		Role saved = roleRepository.save(Role.builder().name(request.getName()).build());
		
		return toResponse(saved);
	}

	private RoleResponse toResponse(Role role) {
		// TODO Auto-generated method stub
		
		return new RoleResponse(role.getId(), role.getName());
	}

	@Override
	public List<RoleResponse> getAllRoles() {
		// TODO Auto-generated method stub
		
		return roleRepository.findAll().stream()
				.map(this:: toResponse).toList();
	}

	@Override
	public Role getRoleEntityOrThrow(Long roleId) {

	    return roleRepository.findById(roleId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("ROle Not Found with id: " + roleId));
	}

}
