package me.parhamziaei.practice.exception.custom.service;

public class FileStorageServiceException extends RuntimeException {
    public FileStorageServiceException(String message) {
        super(message);
    }
    public FileStorageServiceException(){
        super();
    }
}
