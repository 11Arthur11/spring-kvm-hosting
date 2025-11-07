package me.parhamziaei.practice.dto.response.ticket;

import lombok.*;

import java.util.List;

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
