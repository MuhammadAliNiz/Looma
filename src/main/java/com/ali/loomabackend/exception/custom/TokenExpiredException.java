package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown when a verification token has expired
 */
public class TokenExpiredException extends BaseException {

    public TokenExpiredException(String message) {
        super(ErrorCode.TOKEN_EXPIRED, message);
    }

    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED, "Verification token has expired. Please request a new one.");
    }
}

