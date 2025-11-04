package me.parhamziaei.practice.exception.custom.service;

public class ImageTooLargeException extends FileStorageServiceException {
    public ImageTooLargeException() {
        super("IMAGE SIZE TOO LARGE");
    }
}
