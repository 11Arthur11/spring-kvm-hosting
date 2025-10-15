package me.parhamziaei.practice.exception.custom.authenticate;

public class InvalidTwoFactorException extends AuthenticationException {
    public InvalidTwoFactorException() {
        super("Invalid two-factor");
    }
}
