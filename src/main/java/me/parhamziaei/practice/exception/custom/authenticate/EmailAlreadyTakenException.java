package me.parhamziaei.practice.exception.custom.authenticate;

public class EmailAlreadyTakenException extends AuthenticationException{
    public EmailAlreadyTakenException() {
        super("EMAIL_ALREADY_TAKEN");
    }
}
