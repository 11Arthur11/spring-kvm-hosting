package me.parhamziaei.practice.exception.custom.service;

public class ImageNotFoundException extends FileStorageServiceException {
    public ImageNotFoundException() {
        super("Image not found");
    }
}
