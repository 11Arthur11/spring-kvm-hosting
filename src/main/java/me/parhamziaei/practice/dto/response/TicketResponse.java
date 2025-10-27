package me.parhamziaei.practice.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Getter
@Setter
@Builder
public class TicketResponse {

    private Long id;

    private String subject;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime lastModified;

}