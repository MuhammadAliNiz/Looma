package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown when a deleted account is accessed
 */
public class AccountDeletedException extends BaseException {

    public AccountDeletedException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }

    public AccountDeletedException() {
        super(ErrorCode.USER_NOT_FOUND, "Account does not exist.");
    }
}

