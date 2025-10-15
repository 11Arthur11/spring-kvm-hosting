package me.parhamziaei.practice.exception.handler;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.authenticate.EmailAlreadyTakenException;
import me.parhamziaei.practice.exception.custom.authenticate.PasswordPolicyException;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class RegistrationExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<Object> handleEmailAlreadyTakenException(EmailAlreadyTakenException e) {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.AUTH_ALREADY_LOGGED_IN),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PasswordPolicyException.class)
    public ResponseEntity<Object> handlePasswordPolicyException(PasswordPolicyException e) {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVER_VALIDATION_ERROR),
                HttpStatus.BAD_REQUEST
        );
    }

}
