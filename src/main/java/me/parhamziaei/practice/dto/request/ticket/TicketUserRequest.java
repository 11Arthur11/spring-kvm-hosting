package me.parhamziaei.practice.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.validation.annotation.EnumValue;
import org.hibernate.validator.constraints.Length;

@Data
public class TicketUserRequest implements TicketBaseRequest{

    @NotBlank
    @Length(min = 5)
    private String subject;

    @EnumValue(enumClass = TicketDepartment.class)
    private String department;

    private String serviceName;

    private TicketMessageRequest message;

    @Override
    public String getOwnerEmail() {
        return null;
    }
}

