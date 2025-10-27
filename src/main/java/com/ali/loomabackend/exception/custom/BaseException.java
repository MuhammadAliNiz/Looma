package com.ali.loomabackend.exception.custom;

import com.ali.loomabackend.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object[] messageArgs;
    
    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageArgs = null;
    }
    
    protected BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }
    
    protected BaseException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }
    
    protected BaseException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }
}

