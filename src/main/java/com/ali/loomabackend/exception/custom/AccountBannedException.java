package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown when a banned user tries to login
 */
public class AccountBannedException extends BaseException {

    public AccountBannedException(String message) {
        super(ErrorCode.ACCOUNT_DISABLED, message);
    }

    public AccountBannedException() {
        super(ErrorCode.ACCOUNT_DISABLED, "Your account has been banned. Please contact support.");
    }
}

