package me.parhamziaei.practice.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.parhamziaei.practice.entity.jpa.Ticket;

import java.util.Set;

@Data
@Getter
@Setter
@Builder
public class TicketDetailResponse {

    private String subject;

    private String status;

    private String department;

    private String serviceName;

    private Set<TicketMessageResponse> messages;

}
