package com.app.rbac.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.app.rbac.service.SecureDataService;


@Service
public class SecureDataServiceImpl implements SecureDataService{

	@Override
	public Map<String, Object> getSecureData() {
		// TODO Auto-generated method stub
		String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
		
		
		return Map.of(
				"message", "Access granted to protected resouece",
				"requestBy", currentUser,
				"servedAt", Instant.now().toString(),
				"date", List.of("record-1", "record-2","record-3")
				);
	}
}
