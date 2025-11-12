package me.parhamziaei.practice.dto.request.ticket;

public interface TicketBaseRequest {
    String getSubject();

    String getDepartment();

    String getServiceName();

    String getOwnerEmail();

    TicketMessageRequest getMessage();
}
