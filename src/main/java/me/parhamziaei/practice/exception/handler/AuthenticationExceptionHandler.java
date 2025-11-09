package me.parhamziaei.practice.exception.handler;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.authenticate.AlreadyLoggedInException;
import me.parhamziaei.practice.exception.custom.authenticate.EmailAlreadyTakenException;
import me.parhamziaei.practice.exception.custom.authenticate.InvalidTwoFactorException;
import me.parhamziaei.practice.exception.custom.authenticate.PasswordPolicyException;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class AuthenticationExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentialsException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.AUTH_BAD_CREDENTIALS),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> usernameNotFoundException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.AUTH_ACCOUNT_NOT_FOUND),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> lockedException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.AUTH_ACCOUNT_LOCKED),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AlreadyLoggedInException.class)
    public ResponseEntity<Object> alreadyLoggedInException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.AUTH_ALREADY_LOGGED_IN),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InvalidTwoFactorException.class)
    public ResponseEntity<Object> invalidTwoFactorException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.TWO_FACTOR_INVALID),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> disabledException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.AUTH_ACCOUNT_DISABLED),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<Object> handleEmailAlreadyTakenException(EmailAlreadyTakenException e) {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.REGISTER_EMAIL_ALREADY_TAKEN),
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
