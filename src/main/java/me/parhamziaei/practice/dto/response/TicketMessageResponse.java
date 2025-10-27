package me.parhamziaei.practice.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
public class TicketMessageResponse {

    private String senderFullName;

    private LocalDateTime sentAt;

    private String message;

}
