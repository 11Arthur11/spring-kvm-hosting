package me.parhamziaei.practice.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.service.*;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> generalException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVER_INTERNAL_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> noResourceFoundException(HttpServletResponse response, HttpServletRequest request) {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVER_RESOURCE_NOT_FOUND),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> ioException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVER_IO_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
//        String validationError = Optional.ofNullable(exception.getBindingResult().getFieldError())
//                .map(FieldError::getDefaultMessage)
//                .orElse("REQUEST_ARGUMENT_ERROR");

        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVER_VALIDATION_ERROR),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(TicketServiceException.class)
    public ResponseEntity<Object> handleTicketServiceException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.DEFAULT_FAILED),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(TicketMaxAttachmentReachedException.class)
    public ResponseEntity<Object> handleTicketMaxAttachmentReachedException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVICE_TICKET_MAX_ATTACHMENT_REACHED),
                HttpStatus.BAD_REQUEST
        );
    }

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
