package me.parhamziaei.practice.exception.custom.service;

public class TicketMaxAttachmentReachedException extends TicketServiceException {
    public TicketMaxAttachmentReachedException(String message) {
        super(message);
    }
}
