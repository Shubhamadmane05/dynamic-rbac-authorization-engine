package com.app.rbac.security;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.app.rbac.repository.UserRoleRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPermissionEvaluatorTest {

    @Mock
    private UserRoleRepository userRoleMappingRepository;

    @InjectMocks
    private CustomPermissionEvaluator permissionEvaluator;

    private Authentication authenticatedAs(String username) {
        return new UsernamePasswordAuthenticationToken(username, "N/A", List.of());
    }

    @BeforeEach
    void setUp() {
        // Nothing needed for @InjectMocks with a single-arg constructor from @RequiredArgsConstructor.
    }

    @Test
    void grantsAccess_whenUserHasExactPermission() {
        when(userRoleMappingRepository.findPermissionNamesByUsername("admin"))
                .thenReturn(Set.of("ROLE_CREATE", "SECURE_DATA_READ"));

        boolean result = permissionEvaluator.hasPermission(authenticatedAs("admin"), "ROLE", "CREATE");

        assertThat(result).isTrue();
    }

    @Test
    void deniesAccess_whenUserLacksPermission() {
        when(userRoleMappingRepository.findPermissionNamesByUsername("john"))
                .thenReturn(Set.of("SECURE_DATA_READ"));

        boolean result = permissionEvaluator.hasPermission(authenticatedAs("john"), "ROLE", "CREATE");

        assertThat(result).isFalse();
    }

    @Test
    void isCaseInsensitive_forTargetTypeAndAction() {
        when(userRoleMappingRepository.findPermissionNamesByUsername("admin"))
                .thenReturn(Set.of("SECURE_DATA_READ"));

        boolean result = permissionEvaluator.hasPermission(authenticatedAs("admin"), "secure_data", "read");

        assertThat(result).isTrue();
    }

    @Test
    void deniesAccess_whenAuthenticationIsNull() {
        boolean result = permissionEvaluator.hasPermission(null, "ROLE", "CREATE");

        assertThat(result).isFalse();
    }

    @Test
    void deniesAccess_whenAuthenticationIsNotAuthenticated() {
        UsernamePasswordAuthenticationToken unauthenticated =
                new UsernamePasswordAuthenticationToken("ghost", "N/A");
        unauthenticated.setAuthenticated(false);

        boolean result = permissionEvaluator.hasPermission(unauthenticated, "ROLE", "CREATE");

        assertThat(result).isFalse();
    }

    @Test
    void deniesAccess_whenTargetOrPermissionIsNull() {
        Authentication auth = authenticatedAs("admin");

        assertThat(permissionEvaluator.hasPermission(auth, null, "CREATE")).isFalse();
        assertThat(permissionEvaluator.hasPermission(auth, "ROLE", null)).isFalse();
    }

    @Test
    void threeArgOverload_delegatesToTwoArgOverload() {
        when(userRoleMappingRepository.findPermissionNamesByUsername("admin"))
                .thenReturn(Set.of("USER_ROLE_ASSIGN"));

        boolean result = permissionEvaluator.hasPermission(authenticatedAs("admin"), 42L, "USER_ROLE", "ASSIGN");

        assertThat(result).isTrue();
    }
}
