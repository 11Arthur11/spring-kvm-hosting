package me.parhamziaei.practice.dto.response.ticket;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDetailBaseResponse extends  AbstractTicketResponse {
    
    private String subject;

    private String serviceName;

    private LocalDateTime createdAt;

    private LocalDateTime lastModified;

    private List<TicketMessageResponse> messages;
    
}
