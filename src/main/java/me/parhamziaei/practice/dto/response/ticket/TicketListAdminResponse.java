package me.parhamziaei.practice.dto.response.ticket;

import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketListAdminResponse extends TicketBaseResponse {

    private String ownerEmail;

    private String ownerFullName;

    private String subject;

    private LocalDateTime createdAt;

    private LocalDateTime lastModified;

}
