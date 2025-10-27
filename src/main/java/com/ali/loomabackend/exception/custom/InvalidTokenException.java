package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown when a token is invalid or expired
 */
public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, message, cause);
    }
}
