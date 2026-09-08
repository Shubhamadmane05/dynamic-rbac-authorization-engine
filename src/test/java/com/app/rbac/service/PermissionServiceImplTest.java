package com.app.rbac.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.rbac.dto.PermissionRequest;
import com.app.rbac.dto.PermissionResponse;
import com.app.rbac.entity.Permission;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.exception.ResourceNotFoundException;
import com.app.rbac.repository.PermissionRepository;
import com.app.rbac.service.impl.PermissionServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void createPermission_savesAndReturns_whenNameIsUnique() {
        when(permissionRepository.existsByName("REPORT_VIEW")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class)))
                .thenReturn(Permission.builder().id(5L).name("REPORT_VIEW").build());

        PermissionResponse response = permissionService.createPermission(new PermissionRequest("REPORT_VIEW"));

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("REPORT_VIEW");
    }

    @Test
    void createPermission_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(permissionRepository.existsByName("SECURE_DATA_READ")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.createPermission(new PermissionRequest("SECURE_DATA_READ")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void getAllPermissions_returnsMappedResponses() {
        when(permissionRepository.findAll()).thenReturn(
                List.of(Permission.builder().id(1L).name("ROLE_CREATE").build()));

        List<PermissionResponse> result = permissionService.getAllPermission();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ROLE_CREATE");
    }

    @Test
    void getPermissionEntityOrThrow_throwsResourceNotFoundException_whenMissing() {
        when(permissionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getPermissionEntityOrThrow(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
