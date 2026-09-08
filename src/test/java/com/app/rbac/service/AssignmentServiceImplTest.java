package com.app.rbac.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.rbac.dto.ApiMessageResponse;
import com.app.rbac.entity.AppUser;
import com.app.rbac.entity.Permission;
import com.app.rbac.entity.Role;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.repository.RolePermissionRepository;
import com.app.rbac.repository.UserRoleRepository;
import com.app.rbac.service.impl.AssignmentServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock private RoleService roleService;
    @Mock private PermissionService permissionService;
    @Mock private AppUserService appUserService;
    @Mock private RolePermissionRepository rolePermissionRepository;
    @Mock private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @Test
    void assignPermissionToRole_succeeds_whenNotAlreadyAssigned() {
        Role role = Role.builder().id(1L).name("MANAGER").build();
        Permission permission = Permission.builder().id(2L).name("REPORT_VIEW").build();

        when(roleService.getRoleEntityOrThrow(1L)).thenReturn(role);
        when(permissionService.getPermissionEntityOrThrow(2L)).thenReturn(permission);
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(1L, 2L)).thenReturn(false);

        ApiMessageResponse response = assignmentService.assignPermissionToRole(1L, 2L);

        assertThat(response.getMessage()).contains("REPORT_VIEW").contains("MANAGER");
        verify(rolePermissionRepository).save(any());
    }

    @Test
    void assignPermissionToRole_throwsDuplicate_whenAlreadyAssigned() {
        Role role = Role.builder().id(1L).name("MANAGER").build();
        Permission permission = Permission.builder().id(2L).name("REPORT_VIEW").build();

        when(roleService.getRoleEntityOrThrow(1L)).thenReturn(role);
        when(permissionService.getPermissionEntityOrThrow(2L)).thenReturn(permission);
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> assignmentService.assignPermissionToRole(1L, 2L))
                .isInstanceOf(DuplicateResourceException.class);

        verify(rolePermissionRepository, never()).save(any());
    }

    @Test
    void assignRoleToUser_succeeds_whenNotAlreadyAssigned() {
        AppUser user = AppUser.builder().id(10L).username("alice").build();
        Role role = Role.builder().id(1L).name("MANAGER").build();

        when(appUserService.getUserEntityOrThrow(10L)).thenReturn(user);
        when(roleService.getRoleEntityOrThrow(1L)).thenReturn(role);
        when(userRoleRepository.existsByUserIdAndRoleId(10L, 1L)).thenReturn(false);

        ApiMessageResponse response = assignmentService.assignRoleToUser(10L, 1L);

        assertThat(response.getMessage()).contains("alice").contains("MANAGER");
        verify(userRoleRepository).save(any());
    }

    @Test
    void assignRoleToUser_throwsDuplicate_whenAlreadyAssigned() {
        AppUser user = AppUser.builder().id(10L).username("alice").build();
        Role role = Role.builder().id(1L).name("MANAGER").build();

        when(appUserService.getUserEntityOrThrow(10L)).thenReturn(user);
        when(roleService.getRoleEntityOrThrow(1L)).thenReturn(role);
        when(userRoleRepository.existsByUserIdAndRoleId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> assignmentService.assignRoleToUser(10L, 1L))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRoleRepository, never()).save(any());
    }
}
