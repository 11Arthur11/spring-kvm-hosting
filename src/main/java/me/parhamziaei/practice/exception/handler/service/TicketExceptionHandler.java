package me.parhamziaei.practice.exception.handler.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.service.TicketMaxAttachmentReachedException;
import me.parhamziaei.practice.exception.custom.service.TicketServiceException;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TicketService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class TicketExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(TicketServiceException.class)
    public ResponseEntity<?> handleTicketServiceException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.DEFAULT_FAILED),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(TicketMaxAttachmentReachedException.class)
    public ResponseEntity<?> handleTicketMaxAttachmentReachedException() {
        return ResponseBuilder.buildFailed(
                "ERROR",
                messageService.get(Message.SERVICE_TICKET_MAX_ATTACHMENT_REACHED),
                HttpStatus.BAD_REQUEST
        );
    }

}
