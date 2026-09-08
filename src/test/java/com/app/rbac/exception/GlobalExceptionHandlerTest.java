package com.app.rbac.exception;


import com.app.rbac.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesResourceNotFound_as404() {
        when(request.getRequestURI()).thenReturn("/roles/99");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFoundEx(new ResourceNotFoundException("Role not found with id: 99"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("99");
        assertThat(response.getBody().getPath()).isEqualTo("/roles/99");
    }

    @Test
    void handlesDuplicateResource_as409() {
        when(request.getRequestURI()).thenReturn("/roles");

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateEx(new DuplicateResourceException("Role already exists: ADMIN"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handlesAccessDenied_as403WithGenericMessage() {
        when(request.getRequestURI()).thenReturn("/secure-data");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDeniedEx(new AccessDeniedException("denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).contains("permission");
    }

    @Test
    void handlesBadCredentials_as401() {
        when(request.getRequestURI()).thenReturn("/secure-data");

        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("bad creds"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handlesIllegalArgument_as400() {
        when(request.getRequestURI()).thenReturn("/roles");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentEX(new IllegalArgumentException("bad input"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("bad input");
    }

    @Test
    void handlesGenericException_as500WithoutLeakingInternals() {
        when(request.getRequestURI()).thenReturn("/roles");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(new RuntimeException("some internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("some internal detail");
    }
}
