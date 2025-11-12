package me.parhamziaei.practice.dto.response.ticket;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TicketDetailAdminResponse extends TicketDetailBaseResponse {

    private String ownerEmail;

    private String ownerFullName;

}
