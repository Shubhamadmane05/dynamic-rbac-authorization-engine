package com.app.rbac.service;

import java.util.List;

import com.app.rbac.dto.AppUserResponse;
import com.app.rbac.dto.RegisterUserRequest;
import com.app.rbac.entity.AppUser;

public interface AppUserService {

	AppUserResponse registerUser(RegisterUserRequest registerUserRequest);
	AppUser getUserEntityOrThrow(Long userId);
	
	List<AppUserResponse> getAllUsers();
}
