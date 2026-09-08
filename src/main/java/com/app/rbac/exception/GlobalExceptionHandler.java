package com.app.rbac.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.app.rbac.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundEx(ResourceNotFoundException ex, HttpServletRequest request){
		
		
		return build(HttpStatus.NOT_FOUND, ex.getMessage(),request, null );
	}
	
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEx(DuplicateResourceException ex, HttpServletRequest request){
		return build(HttpStatus.CONFLICT, ex.getMessage(),request, null );
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
		
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> ((FieldError) err).getField()+": "+ err.getDefaultMessage())
				
				.toList();
						
				return build(HttpStatus.BAD_REQUEST, "Request validaion failed", request, details);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedEx(AccessDeniedException ex, HttpServletRequest request){
		return build(HttpStatus.FORBIDDEN, "You dont have the require permission to perform this action",request, null );
	}
	
	 @ExceptionHandler(IllegalArgumentException.class)
	    public ResponseEntity<ErrorResponse> handleIllegalArgumentEX(IllegalArgumentException ex, HttpServletRequest request) {
	        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
	    }


	    @ExceptionHandler(BadCredentialsException.class)
	    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
	        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", request, null);
	    }
	 
	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request, List<String> details){
		ErrorResponse body = ErrorResponse.builder()
			    .timestamp(LocalDateTime.now())
			    .status(status.value())
			    .error(status.getReasonPhrase())   
			    .message(message)                  
			    .path(request.getRequestURI())
			    .details(details)
			    .build();
		return ResponseEntity.status(status).body(body);
	}
	
	  @ExceptionHandler(Exception.class)
	    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
	        log.error("Unexpected error at {}", request.getRequestURI(), ex);   // <-- ADD THIS LINE
	        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
	    }
}
