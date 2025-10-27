package com.ali.loomabackend.exception.custom;

import com.ali.loomabackend.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public class AccountNotVerifiedException extends BaseException {

    private final String accessToken;
    private final String refreshToken;

    public AccountNotVerifiedException(String message) {
        super(ErrorCode.EMAIL_NOT_VERIFIED, message);
        this.accessToken = null;
        this.refreshToken = null;
    }

    public AccountNotVerifiedException() {
        super(ErrorCode.EMAIL_NOT_VERIFIED, "Account not verified. Please verify your email address.");
        this.accessToken = null;
        this.refreshToken = null;
    }

    public AccountNotVerifiedException(String message, String accessToken, String refreshToken) {
        super(ErrorCode.EMAIL_NOT_VERIFIED, message);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}