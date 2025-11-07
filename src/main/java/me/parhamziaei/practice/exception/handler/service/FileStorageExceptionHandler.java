package me.parhamziaei.practice.exception.handler.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.service.*;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class FileStorageExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(FileStorageServiceException.class)
    public ResponseEntity<?> handleFileStorageService() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVICE_FILE_STORAGE_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MediaTypeNotAllowedException.class)
    public ResponseEntity<?> handleUnsupportedMediaTypeException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVICE_MEDIA_TYPE_NOT_ALLOWED),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE
        );
    }

    @ExceptionHandler(MediaSizeTooLargeException.class)
    public ResponseEntity<?> handleMediaSizeTooLargeException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVICE_PAYLOAD_TOO_LARGE),
                HttpStatus.PAYLOAD_TOO_LARGE
        );
    }



}
