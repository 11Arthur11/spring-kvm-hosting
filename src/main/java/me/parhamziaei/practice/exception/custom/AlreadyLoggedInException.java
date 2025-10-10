package me.parhamziaei.practice.exception.custom;

public class AlreadyLoggedInException extends RuntimeException{
    public AlreadyLoggedInException(String message) {
        super(message);
    }
    public AlreadyLoggedInException() {
        super();
    }
}
