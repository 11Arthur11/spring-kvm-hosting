package me.parhamziaei.practice.exception.custom.authenticate;

public class AlreadyLoggedInException extends RuntimeException{
    public AlreadyLoggedInException() {
        super("ALREADY_LOGGED_IN");
    }
}
