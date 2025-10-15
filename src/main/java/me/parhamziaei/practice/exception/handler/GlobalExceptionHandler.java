package me.parhamziaei.practice.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.Optional;

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

}
