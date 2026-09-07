package com.app.rbac.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.rbac.dto.AppUserResponse;
import com.app.rbac.dto.RegisterUserRequest;
import com.app.rbac.entity.AppUser;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.exception.ResourceNotFoundException;
import com.app.rbac.repository.AppUserRepository;
import com.app.rbac.service.AppUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService{

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	
	@Override
	public AppUserResponse registerUser(RegisterUserRequest registerUserRequest) {
		
		if(appUserRepository.existsByUsername(registerUserRequest.getUsername())) {
			throw new DuplicateResourceException("UserName Alreday Taken" + registerUserRequest.getUsername());
		}
		
		AppUser user = AppUser.builder()
				.username(registerUserRequest.getUsername())
				.password(passwordEncoder.encode(registerUserRequest.getPassword()))
				.enabled(true)
				.build();
		
		AppUser saved =appUserRepository.save(user);
		return new AppUserResponse(saved.getId(), saved.getUsername(),saved.isEnabled());
	}

	 @Override
	    public AppUser getUserEntityOrThrow(Long userId) {
	        return appUserRepository.findById(userId)
	                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
	    }

	@Override
	public List<AppUserResponse> getAllUsers() {
	    return appUserRepository.findAll().stream()
	            .map(user -> new AppUserResponse(user.getId(), user.getUsername(), user.isEnabled()))
	            .toList();
	}
}
