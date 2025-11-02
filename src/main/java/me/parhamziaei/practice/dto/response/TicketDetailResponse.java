package me.parhamziaei.practice.dto.response;

import lombok.*;
import me.parhamziaei.practice.entity.jpa.Ticket;

import java.util.List;
import java.util.Set;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDetailResponse {

    private Long id;

    private String subject;

    private String status;

    private String department;

    private String serviceName;

    private List<TicketMessageResponse> messages;

}
