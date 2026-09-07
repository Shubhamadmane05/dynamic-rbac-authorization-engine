package com.app.rbac.security;

import java.io.Serializable;
import java.util.Set;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.app.rbac.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator{

	private final UserRoleRepository userRoleRepository;
	
	   @Override
	    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
	        if (authentication == null || !authentication.isAuthenticated()
	                || targetDomainObject == null || permission == null) {
	            return false;
	        }

	        String requiredPermission = buildPermissionName(targetDomainObject.toString(), permission.toString());
	        Set<String> grantedPermissions = resolveUserPermissions(authentication.getName());

	        return grantedPermissions.contains(requiredPermission);
	    }
	
	 @Override
	    public boolean hasPermission(Authentication authentication, Serializable targetId,
	                                  String targetType, Object permission) {
	        return hasPermission(authentication, targetType, permission);
	    }

	    private String buildPermissionName(String targetType, String action) {
	        return targetType.trim().toUpperCase() + "_" + action.trim().toUpperCase();
	    }
	    

	    public Set<String> resolveUserPermissions(String username) {
	        return userRoleRepository.findPermissionNamesByUsername(username);
	    }
}
