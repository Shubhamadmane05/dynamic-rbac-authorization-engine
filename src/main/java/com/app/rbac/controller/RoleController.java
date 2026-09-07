package com.app.rbac.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.rbac.dto.ApiMessageResponse;
import com.app.rbac.dto.RoleRequest;
import com.app.rbac.dto.RoleResponse;
import com.app.rbac.service.AssignmentService;
import com.app.rbac.service.RoleService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleService roleService;
	private final AssignmentService assignmentService;
	
	 @PostMapping
	    @PreAuthorize("hasPermission('ROLE', 'CREATE')")
	    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
	        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
	    }

	    
	    @GetMapping
	    @PreAuthorize("hasPermission('ROLE', 'READ')")
	    public ResponseEntity<List<RoleResponse>> getAllRoles() {
	        return ResponseEntity.ok(roleService.getAllRoles());
	    }
	    
	    @PostMapping("/{roleId}/permissions/{permissionId}")
	    @PreAuthorize("hasPermission('ROLE_PERMISSION', 'ASSIGN')")
	    public ResponseEntity<ApiMessageResponse> assignPermissionToRole(@PathVariable Long roleId,
	                                                                      @PathVariable Long permissionId) {
	        return ResponseEntity.ok(assignmentService.assignPermissionToRole(roleId, permissionId));
	    }
}
