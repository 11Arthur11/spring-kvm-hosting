package me.parhamziaei.practice.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.validation.annotation.EnumValue;
import org.hibernate.validator.constraints.Length;

@Data
public class TicketAdminRequest implements TicketBaseRequest {

    @NotBlank
    @Length(min = 5)
    private String subject;

    @EnumValue(enumClass = TicketDepartment.class)
    private String department;

    private String serviceName;

    @NotBlank
    private String ownerEmail;

    private TicketMessageRequest message;

}
