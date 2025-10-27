package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown for validation errors
 */
public class ValidationException extends BaseException {
    
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ValidationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public static ValidationException invalidData(String fieldName) {
        return new ValidationException(ErrorCode.VALIDATION_ERROR, 
                "Invalid data provided for field: " + fieldName);
    }
    
    public static ValidationException invalidFileType(String allowedTypes) {
        return new ValidationException(ErrorCode.INVALID_FILE_TYPE, 
                "Invalid file type. Allowed types: " + allowedTypes);
    }
    
    public static ValidationException fileTooLarge(long maxSize) {
        return new ValidationException(ErrorCode.FILE_TOO_LARGE, 
                "File size exceeds maximum limit of " + maxSize + " bytes");
    }
}

