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
public class TicketListUserResponse extends TicketBaseResponse {

    private String subject;

    private String status;

    private String department;

    private LocalDateTime createdAt;

    private LocalDateTime lastModified;

}