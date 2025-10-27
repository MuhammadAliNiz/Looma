package com.ali.loomabackend.exception.handler;


import com.ali.loomabackend.exception.custom.*;
import com.ali.loomabackend.model.dto.response.ApiResponse;
import com.ali.loomabackend.model.dto.response.ErrorDetail;
import com.ali.loomabackend.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse; // Keep for cookie setting
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest; // Use WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final CookieUtils cookieUtils;

    // Handle Resource Not Found Exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //Handle custom base exceptions
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex, WebRequest request) {
        log.error("Base exception occurred: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(response);
    }

    //Handle resource already exists exceptions
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, WebRequest request) {
        log.warn("Resource already exists: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }


    //Handle validation exceptions from @Valid annotation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<ErrorDetail> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid input data. Please check the errors and try again.",
                validationErrors,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //Handle constraint violation exceptions
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorDetail> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorDetail.builder()
                        .field(getFieldName(violation))
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        ApiResponse<Object> response = ApiResponse.error(
                "Validation constraint violated",
                validationErrors,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //Handle Spring Security authentication exceptions
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Authentication failed. Please check your credentials.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    //Handle bad credentials exception
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid username or password",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    //Handle insufficient authentication exception
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientAuthentication(
            InsufficientAuthenticationException ex, WebRequest request) {
        log.warn("Insufficient authentication: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Full authentication is required to access this resource",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    //Handle Spring Security access denied exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "You don't have permission to access this resource",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


    //Handle custom authorization exceptions
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationException(
            AuthorizationException ex, WebRequest request) {
        log.warn("Authorization failed: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }


    //Handle business exceptions
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("Business exception: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }


    //Handle HTTP message not readable exceptions
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.error("Malformed JSON request: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Malformed JSON request",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //Handle method argument type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Method argument type mismatch: {}", ex.getMessage());

        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiResponse<Object> response = ApiResponse.error(
                message,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //Handle missing servlet request parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing request parameter: {}", ex.getMessage());

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        ApiResponse<Object> response = ApiResponse.error(
                message,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //Handle unsupported HTTP method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());

        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());

        ApiResponse<Object> response = ApiResponse.error(
                message,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }


    //Handle unsupported media type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.warn("Media type not supported: {}", ex.getMessage());

        String message = String.format("Media type '%s' is not supported", ex.getContentType());

        ApiResponse<Object> response = ApiResponse.error(
                message,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }


    //Handle max upload size exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("Max upload size exceeded: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "File size exceeds the maximum allowed limit",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }


    //Handle no handler found (404)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {
        log.warn("No handler found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "The requested endpoint does not exist",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    //Handle all other uncaught exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: ", ex);

        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    //Handle account not verified exception
    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountNotVerified(
            AccountNotVerifiedException ex,
            WebRequest request,
            HttpServletResponse response) {
        log.warn("Account not verified: {}", ex.getMessage());

        // Set tokens in cookies if available
        if (ex.getAccessToken() != null && ex.getRefreshToken() != null) {
            // Set access token in response header (or cookie if preferred)
            response.addHeader("X-Access-Token", ex.getAccessToken());

            // Set refresh token as HttpOnly cookie
            ResponseCookie refreshTokenCookie = cookieUtils.createHttpOnlyCookie(
                    "refreshToken",
                    ex.getRefreshToken(),
                    7 * 24 * 60 * 60L // 7 days
            );
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            log.info("Tokens set in cookies for unverified account");
        }

        // Create custom data map with tokens (exclude refreshToken from response body)
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("verified", false);
        if (ex.getAccessToken() != null) {
            additionalInfo.put("accessToken", ex.getAccessToken());
        }
        // DO NOT include refreshToken in response body - it's only in HTTP-only cookie

        // Use the ApiResponse builder to set the 'data' field
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .path(getPath(request))
                .timestamp(LocalDateTime.now())
                .data(additionalInfo) // Put the additional info map in the 'data' field
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    //Handle account banned exception
    @ExceptionHandler(AccountBannedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountBanned(
            AccountBannedException ex, WebRequest request) {
        log.warn("Account banned: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    //Handle account deleted exception
    @ExceptionHandler(AccountDeletedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountDeleted(
            AccountDeletedException ex, WebRequest request) {
        log.warn("Account deleted: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    //Handle token expired exception
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenExpired(
            TokenExpiredException ex, WebRequest request) {
        log.warn("Token expired: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    //Handle invalid token exception
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(
            InvalidTokenException ex, WebRequest request) {
        log.warn("Invalid token: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    //Handle username not found exception
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFound(
            UsernameNotFoundException ex, WebRequest request) {
        log.warn("Username not found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid username or password",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    //Handle disabled account exception
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(
            DisabledException ex, WebRequest request) {
        log.warn("Account disabled: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Account has been disabled. Please contact support.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    //Handle locked account exception
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(
            LockedException ex, WebRequest request) {
        log.warn("Account locked: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Account has been locked. Please contact support.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    //Handle data integrity violation exception
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation occurred";

        // Parse common constraint violations
        if (ex.getMessage() != null) {
            String errorMsg = ex.getMessage().toLowerCase();
            if (errorMsg.contains("duplicate") || errorMsg.contains("unique")) {
                message = "A record with the same unique field already exists";
            } else if (errorMsg.contains("foreign key")) {
                message = "Referenced record does not exist";
            } else if (errorMsg.contains("not-null") || errorMsg.contains("null value")) {
                message = "Required field is missing";
            }
        }

        ApiResponse<Object> response = ApiResponse.error(
                message,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //Extract field name from constraint violation
    private String getFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }

    // Helper method to extract path from request
    private String getPath(WebRequest request) {
        // "uri=" is part of the description, remove it
        return request.getDescription(false).replace("uri=", "");
    }
}