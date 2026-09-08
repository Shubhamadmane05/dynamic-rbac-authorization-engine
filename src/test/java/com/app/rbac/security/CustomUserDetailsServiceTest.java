package com.app.rbac.security;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.app.rbac.entity.AppUser;
import com.app.rbac.repository.AppUserRepository;
import com.app.rbac.repository.UserRoleRepository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_buildsUserDetailsWithRolePrefixedAuthorities() {
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(
                AppUser.builder().id(1L).username("admin").password("hashed").enabled(true).build()));
        when(userRoleRepository.findRoleNamesByUsername("admin")).thenReturn(Set.of("ADMIN"));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("hashed");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_marksDisabled_whenAccountDisabled() {
        when(appUserRepository.findByUsername("bob")).thenReturn(Optional.of(
                AppUser.builder().id(2L).username("bob").password("hashed").enabled(false).build()));
        when(userRoleRepository.findRoleNamesByUsername("bob")).thenReturn(Set.of());

        UserDetails userDetails = userDetailsService.loadUserByUsername("bob");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenMissing() {
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
