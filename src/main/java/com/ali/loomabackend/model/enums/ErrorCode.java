package com.ali.loomabackend.model.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // General errors (1xxx)
    INTERNAL_SERVER_ERROR(1000, "Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(1001, "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(1002, "Validation failed", HttpStatus.BAD_REQUEST),


    // Resource errors (2xxx)
    RESOURCE_NOT_FOUND(2000, "Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS(2001, "Resource already exists", HttpStatus.CONFLICT),
    RESOURCE_CONFLICT(2002, "Resource conflict", HttpStatus.CONFLICT),


    // Authentication errors (3xxx)
    AUTHENTICATION_FAILED(3000, "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(3001, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(3002, "Authentication token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(3003, "Invalid authentication token", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS(3004, "Unauthorized access", HttpStatus.UNAUTHORIZED),


    // Authorization errors (4xxx)
    ACCESS_DENIED(4000, "Access denied", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS(4001, "Insufficient permissions", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(4002, "Account has been disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(4003, "Account has been locked", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(4004, "Email address not verified", HttpStatus.FORBIDDEN),


    // User errors (5xxx)
    USER_NOT_FOUND(5000, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(5001, "User already exists", HttpStatus.CONFLICT),
    INVALID_USER_DATA(5002, "Invalid user data", HttpStatus.BAD_REQUEST),


    // Post errors (6xxx)
    POST_NOT_FOUND(6000, "Post not found", HttpStatus.NOT_FOUND),
    INVALID_POST_DATA(6001, "Invalid post data", HttpStatus.BAD_REQUEST),
    POST_ALREADY_DELETED(6002, "Post already deleted", HttpStatus.GONE),


    // Comment errors (7xxx)
    COMMENT_NOT_FOUND(7000, "Comment not found", HttpStatus.NOT_FOUND),
    INVALID_COMMENT_DATA(7001, "Invalid comment data", HttpStatus.BAD_REQUEST),
    COMMENT_ALREADY_DELETED(7002, "Comment already deleted", HttpStatus.GONE),


    // Media errors (8xxx)
    MEDIA_NOT_FOUND(8000, "Media not found", HttpStatus.NOT_FOUND),
    INVALID_FILE_TYPE(8001, "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(8002, "File size exceeds maximum limit", HttpStatus.BAD_REQUEST),
    MEDIA_UPLOAD_FAILED(8003, "Media upload failed", HttpStatus.INTERNAL_SERVER_ERROR),


    // Follow errors (9xxx)
    FOLLOW_NOT_FOUND(9000, "Follow relationship not found", HttpStatus.NOT_FOUND),
    ALREADY_FOLLOWING(9001, "Already following this user", HttpStatus.CONFLICT),
    CANNOT_FOLLOW_SELF(9002, "Cannot follow yourself", HttpStatus.BAD_REQUEST),


    // Notification errors (10xxx)
    NOTIFICATION_NOT_FOUND(10000, "Notification not found", HttpStatus.NOT_FOUND),


    // Reaction errors (11xxx)
    REACTION_NOT_FOUND(11000, "Reaction not found", HttpStatus.NOT_FOUND),
    REACTION_ALREADY_EXISTS(11001, "Reaction already exists", HttpStatus.CONFLICT);


    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}

