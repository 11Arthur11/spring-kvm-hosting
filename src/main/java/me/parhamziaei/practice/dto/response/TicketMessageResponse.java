package me.parhamziaei.practice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketMessageResponse {

    private String senderFullName;

    private LocalDateTime sentAt;

    private String message;

    private String senderRole;

    private Set<TicketAttachmentResponse> attachments;

}
