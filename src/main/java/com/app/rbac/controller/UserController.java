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
import com.app.rbac.dto.AppUserResponse;
import com.app.rbac.dto.RegisterUserRequest;
import com.app.rbac.service.AppUserService;
import com.app.rbac.service.AssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	 private final AppUserService appUserService;
	    private final AssignmentService assignmentService;


	    @PostMapping("/register")
	    public ResponseEntity<AppUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
	        return ResponseEntity.status(HttpStatus.CREATED).body(appUserService.registerUser(request));
	    }

	    @PostMapping("/{userId}/roles/{roleId}")
	    @PreAuthorize("hasPermission('USER_ROLE', 'ASSIGN')")
	    public ResponseEntity<ApiMessageResponse> assignRoleToUser(@PathVariable Long userId,
	                                                                @PathVariable Long roleId) {
	        return ResponseEntity.ok(assignmentService.assignRoleToUser(userId, roleId));
	    }
	    
	    @GetMapping
	    @PreAuthorize("hasPermission('USER', 'READ')")
	    public ResponseEntity<List<AppUserResponse>> getAllUsers() {
	        return ResponseEntity.ok(appUserService.getAllUsers());
	    }
}
