package com.app.rbac.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.rbac.dto.AppUserResponse;
import com.app.rbac.dto.RegisterUserRequest;
import com.app.rbac.entity.AppUser;
import com.app.rbac.exception.DuplicateResourceException;
import com.app.rbac.exception.ResourceNotFoundException;
import com.app.rbac.repository.AppUserRepository;
import com.app.rbac.service.impl.AppUserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    @Test
    void registerUser_encodesPasswordAndSaves_whenUsernameIsFree() {
        when(appUserRepository.existsByUsername("rahul")).thenReturn(false);
        when(passwordEncoder.encode("rahul123")).thenReturn("hashed-pass");
        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(AppUser.builder().id(7L).username("rahul").password("hashed-pass").enabled(true).build());

        AppUserResponse response = appUserService.registerUser(new RegisterUserRequest("rahul", "rahul123"));

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("rahul");
        assertThat(response.isEnabled()).isTrue();
        verify(passwordEncoder).encode("rahul123");
    }

    @Test
    void registerUser_throwsDuplicateResourceException_whenUsernameTaken() {
        when(appUserRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> appUserService.registerUser(new RegisterUserRequest("admin", "admin123")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void getUserEntityOrThrow_throwsResourceNotFoundException_whenMissing() {
        when(appUserRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getUserEntityOrThrow(123L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
