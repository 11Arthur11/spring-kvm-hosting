package me.parhamziaei.practice.dto.response.ticket;

import lombok.Data;

@Data
public abstract class TicketBaseResponse {
    protected Long id;
    protected String status;
    protected String department;
}
