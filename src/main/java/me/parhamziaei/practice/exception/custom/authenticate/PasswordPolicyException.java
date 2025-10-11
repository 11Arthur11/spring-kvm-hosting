package me.parhamziaei.practice.exception.custom.authenticate;

public class PasswordPolicyException extends AuthenticationException{
    public PasswordPolicyException(String message) {
        super(message);
    }
}
