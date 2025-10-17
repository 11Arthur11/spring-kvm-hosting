package me.parhamziaei.practice.exception.custom.authenticate;

public class InvalidEmailVerifyCodeException extends AuthenticationException {
    public InvalidEmailVerifyCodeException() {
        super("Email verification code is invalid");
    }
}
