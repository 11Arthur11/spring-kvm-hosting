package me.parhamziaei.practice.exception.custom.service;

public class NoSuchDataException extends RuntimeException {
    public NoSuchDataException(String message) {
        super(message);
    }
    public NoSuchDataException() {
        super();
    }
}
