package me.parhamziaei.practice.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Data
public class TicketUserRequest implements TicketBaseRequest{

    @NotBlank
    @Length(min = 5)
    private String subject;

    @NotBlank
    private String department;

    private String serviceName;

    private TicketMessageRequest message;

    @Override
    public String getOwnerEmail() {
        return null;
    }
}

