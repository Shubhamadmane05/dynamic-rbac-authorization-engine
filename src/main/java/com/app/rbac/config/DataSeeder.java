package com.app.rbac.config;


import java.lang.module.ModuleDescriptor.Builder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.app.rbac.entity.AppUser;
import com.app.rbac.entity.Permission;
import com.app.rbac.entity.Role;
import com.app.rbac.entity.RolePermission;
import com.app.rbac.entity.UserRole;
import com.app.rbac.repository.AppUserRepository;
import com.app.rbac.repository.PermissionRepository;
import com.app.rbac.repository.RolePermissionRepository;
import com.app.rbac.repository.RoleRepository;
import com.app.rbac.repository.UserRoleRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner{
	
	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final AppUserRepository appUserRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Value("${rbac.seed.enabled:treu}")
	private boolean seedEnabled;
	
	private static final List<String> ALL_PERMISSIONS = List.of(
			"ROLE_CREATE", "ROLE_READ",
			"PERMISSION_CREATE","PERMISSION_READ",
			"ROLE_PERMISSION_ASSIGN",
			"USER_ROLE_ASSIGN",
			 "USER_READ",
			"SECURE_DATA_READ"
			);
	
	public void run(String... args) {
		if(!seedEnabled || roleRepository.count() > 0) {
			return;
		}
		
		log.info("Seeding initial RBAC data (ADMIN role, USER role, permissions, admin account)...");
		Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
		Role userRole =roleRepository.save(Role.builder().name("USER").build());
		
		Map<String, Permission> permissions = ALL_PERMISSIONS.stream()
		        .collect(Collectors.toMap(
		                name -> name,
		                name -> permissionRepository.save(
		                        Permission.builder()
		                                .name(name)
		                                .build()
		                )
		        ));
		
		permissions.values().forEach(permission ->
				rolePermissionRepository.save(RolePermission.builder().role(adminRole).permission(permission).build())
						
				);
		
		rolePermissionRepository.save(RolePermission.builder()
				.role(userRole).permission(permissions.get("SECURE_DATA_READ")).build());
		
		AppUser admin = appUserRepository.save(AppUser.builder()
				.username("admin1")
				.password(passwordEncoder.encode("admin123"))
				.enabled(true)
				.build()
				);
		AppUser demoUser = appUserRepository.save(AppUser.builder()
				.username("shubham")
				.password(passwordEncoder.encode("shubham123"))
				.enabled(true)
				.build()
				);
		
		userRoleRepository.save(UserRole.builder().user(admin).role(adminRole).build());
		userRoleRepository.save(UserRole.builder().user(demoUser).role(userRole).build());
		
		log.info("Seed complete. Default credentials -> admin1/admin123 (ADMIN), shubham/shubham123 (USER).");
				
	}
}
