package me.parhamziaei.practice.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class TicketAdminRequest implements TicketBaseRequest {

    @NotBlank
    @Length(min = 5)
    private String subject;

    @NotBlank
    private String department;

    private String serviceName;

    @NotBlank
    private String ownerEmail;

    private TicketMessageRequest message;

}
