package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown for business logic violations
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public static BusinessException cannotFollowSelf() {
        return new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
    }
    
    public static BusinessException resourceConflict(String message) {
        return new BusinessException(ErrorCode.RESOURCE_CONFLICT, message);
    }
}

