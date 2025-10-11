package me.parhamziaei.practice.exception.handler;

import me.parhamziaei.practice.exception.custom.authenticate.AlreadyLoggedInException;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthenticationExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentialsException() {
        return ResponseBuilder.build(
                "BAD_CREDENTIALS",
                "username or password is incorrect",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> disabledException() {
        return ResponseBuilder.build(
                "ACCOUNT_DISABLED",
                "account is disabled, contact support for details",
                HttpStatus.BAD_REQUEST
                );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> usernameNotFoundException() {
        return ResponseBuilder.build(
                "EMAIL_NOT_FOUND",
                "there is no account with the specified email",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> lockedException() {
        return ResponseBuilder.build(
                "ACCOUNT_LOCKED",
                "account is locked, contact support for details",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AlreadyLoggedInException.class)
    public ResponseEntity<Object> alreadyLoggedInException() {
        return ResponseBuilder.build(
                "ALREADY_LOGGED_IN",
                "operation cannot be performed because user already logged in",
                HttpStatus.CONFLICT
        );
    }

}
