package me.parhamziaei.practice.exception.custom.service;

public class TicketServiceException extends RuntimeException {
    public TicketServiceException(String message) {
        super(message);
    }
    public TicketServiceException(){
        super();
    }
}
