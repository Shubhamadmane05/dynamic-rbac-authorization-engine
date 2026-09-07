package com.app.rbac.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.rbac.dto.PermissionRequest;
import com.app.rbac.dto.PermissionResponse;
import com.app.rbac.service.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/permissions")
@RestController
@RequiredArgsConstructor
public class PermissionController {

	private final PermissionService permissionService;
	
	@PostMapping
	@PreAuthorize("hasPermission('PERMISSION','CREATE')")
	public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request){
		return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.createPermission(request));
	}
	
	@GetMapping
	public ResponseEntity<List<PermissionResponse>> getAllPermission(){
		return ResponseEntity.ok(permissionService.getAllPermission());
	}
	
}
