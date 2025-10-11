package me.parhamziaei.practice.exception.handler;

import me.parhamziaei.practice.exception.custom.authenticate.EmailAlreadyTakenException;
import me.parhamziaei.practice.exception.custom.authenticate.PasswordPolicyException;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RegistrationExceptionHandler {

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<Object> handleEmailAlreadyTakenException(EmailAlreadyTakenException e) {
        return ResponseBuilder.build(
                e.getMessage(),
                "this email is already taken",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PasswordPolicyException.class)
    public ResponseEntity<Object> handlePasswordPolicyException(PasswordPolicyException e) {
        return ResponseBuilder.build(
                e.getMessage(),
                "",
                HttpStatus.BAD_REQUEST
        );
    }

}
