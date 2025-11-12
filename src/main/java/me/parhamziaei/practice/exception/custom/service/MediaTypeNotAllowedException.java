package me.parhamziaei.practice.exception.custom.service;

public class MediaTypeNotAllowedException extends FileStorageServiceException {
    public MediaTypeNotAllowedException(String message) {
        super(message);
    }
    public MediaTypeNotAllowedException() {
        super();
    }
}
