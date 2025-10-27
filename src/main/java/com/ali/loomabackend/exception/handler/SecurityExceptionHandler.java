package com.ali.loomabackend.exception.handler;

import com.ali.loomabackend.model.dto.response.ApiResponse; // Import ApiResponse
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest; // Import WebRequest

import java.time.LocalDateTime;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensures this runs before GlobalExceptionHandler
@RestControllerAdvice
public class SecurityExceptionHandler {


    //Handle authentication service exceptions
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationServiceException(
            AuthenticationServiceException ex, WebRequest request) {
        log.error("Authentication service error: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                "An error occurred during authentication. Please try again.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    //Handle session authentication exceptions
    @ExceptionHandler(SessionAuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleSessionAuthenticationException(
            SessionAuthenticationException ex, WebRequest request) {
        log.warn("Session authentication error: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Your session has expired or is invalid. Please login again.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Helper method to extract path from request
    private String getPath(WebRequest request) {
        // "uri=" is part of the description, remove it
        return request.getDescription(false).replace("uri=", "");
    }
}