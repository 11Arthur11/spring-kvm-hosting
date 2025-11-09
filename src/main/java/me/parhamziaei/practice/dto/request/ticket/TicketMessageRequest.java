package me.parhamziaei.practice.dto.request.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class TicketMessageRequest {

    private String content;

}
