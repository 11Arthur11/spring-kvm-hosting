package me.parhamziaei.practice.dto.response.ticket;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDetailResponse extends TicketBaseResponse {

    private String subject;

    private String serviceName;

    private List<TicketMessageResponse> messages;

}
