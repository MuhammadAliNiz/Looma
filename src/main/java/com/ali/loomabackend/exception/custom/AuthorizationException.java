package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown for authorization failures
 */
public class AuthorizationException extends BaseException {
    
    public AuthorizationException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
    
    public AuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AuthorizationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public static AuthorizationException accessDenied() {
        return new AuthorizationException(ErrorCode.ACCESS_DENIED);
    }
    
    public static AuthorizationException insufficientPermissions() {
        return new AuthorizationException(ErrorCode.INSUFFICIENT_PERMISSIONS);
    }
    
    public static AuthorizationException accountDisabled() {
        return new AuthorizationException(ErrorCode.ACCOUNT_DISABLED);
    }
    
    public static AuthorizationException accountLocked() {
        return new AuthorizationException(ErrorCode.ACCOUNT_LOCKED);
    }
    
    public static AuthorizationException emailNotVerified() {
        return new AuthorizationException(ErrorCode.EMAIL_NOT_VERIFIED);
    }
}

