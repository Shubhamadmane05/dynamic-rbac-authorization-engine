package com.app.rbac.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.rbac.service.SecureDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/secure-data")
@RequiredArgsConstructor
public class SecureDataController {

    private final SecureDataService secureDataService;
	    
        @GetMapping
	    @PreAuthorize("hasPermission('SECURE_DATA', 'READ')")
	    public ResponseEntity<Map<String, Object>> getSecureData() {
	        return ResponseEntity.ok(secureDataService.getSecureData());
	    }
}
