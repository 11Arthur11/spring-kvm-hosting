package me.parhamziaei.practice.exception.custom.authenticate;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
