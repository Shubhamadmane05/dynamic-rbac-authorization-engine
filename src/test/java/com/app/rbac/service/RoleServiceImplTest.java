package com.app.rbac.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.app.rbac.dto.RoleRequest;
import com.app.rbac.dto.RoleResponse;
import com.app.rbac.entity.Role;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.exception.ResourceNotFoundException;
import com.app.rbac.repository.RoleRepository;
import com.app.rbac.service.impl.RoleServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRole_savesAndReturnsRole_whenNameIsUnique() {
        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenReturn(Role.builder().id(1L).name("MANAGER").build());

        RoleResponse response = roleService.createRole(new RoleRequest("MANAGER"));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("MANAGER");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(new RoleRequest("ADMIN")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ADMIN");

        verify(roleRepository, never()).save(any());
    }

    @Test
    void getAllRoles_returnsMappedResponses() {
        when(roleRepository.findAll()).thenReturn(List.of(
                Role.builder().id(1L).name("ADMIN").build(),
                Role.builder().id(2L).name("USER").build()
        ));

        List<RoleResponse> result = roleService.getAllRoles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("ADMIN");
    }

    @Test
    void getRoleEntityOrThrow_returnsRole_whenFound() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(Role.builder().id(1L).name("ADMIN").build()));

        Role role = roleService.getRoleEntityOrThrow(1L);

        assertThat(role.getName()).isEqualTo("ADMIN");
    }

    @Test
    void getRoleEntityOrThrow_throwsResourceNotFoundException_whenMissing() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleEntityOrThrow(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
