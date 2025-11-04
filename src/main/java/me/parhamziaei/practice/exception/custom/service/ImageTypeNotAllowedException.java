package me.parhamziaei.practice.exception.custom.service;

public class ImageTypeNotAllowedException extends FileStorageServiceException {
    public ImageTypeNotAllowedException() {
        super("Image type not allowed");
    }
}
