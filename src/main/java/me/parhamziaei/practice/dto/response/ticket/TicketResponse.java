package me.parhamziaei.practice.dto.response.ticket;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;

    private String subject;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime lastModified;

}