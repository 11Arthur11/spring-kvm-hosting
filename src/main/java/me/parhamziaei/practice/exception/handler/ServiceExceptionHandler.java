package me.parhamziaei.practice.exception.handler;

import me.parhamziaei.practice.exception.custom.service.EmailServiceException;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ServiceExceptionHandler {

    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<Object> mailServiceException() {
        return ResponseBuilder.build(
                "MAIL_SERVICE_ERROR",
                "error while sending email",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
