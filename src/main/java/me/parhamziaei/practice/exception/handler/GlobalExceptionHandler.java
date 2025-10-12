package me.parhamziaei.practice.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> generalException() {
        return ResponseBuilder.build(
                "INTERNAL_SERVER_ERROR",
                "general server error, please report this to support",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> noResourceFoundException(HttpServletResponse response, HttpServletRequest request) {
        return ResponseBuilder.build(
                "NOT_FOUND",
                "requested resource not found",
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> ioException() {
        return ResponseBuilder.build(
                "INTERNAL_SERVER_ERROR",
                "error occurred in I/O operation",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String validationError = Optional.ofNullable(exception.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse("REQUEST_ARGUMENT_ERROR");

        return ResponseBuilder.build(
                validationError,
                "server could not validate the format of the submitted fields",
                HttpStatus.BAD_REQUEST
        );
    }

}
