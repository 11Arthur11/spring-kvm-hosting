package me.parhamziaei.practice.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import me.parhamziaei.practice.validation.annotation.EnumValue;
import org.hibernate.validator.constraints.Length;

@Data
@NotNull
public class TicketEditAdminRequest {

    @Length(min = 5)
    private String newSubject;

    @EnumValue(enumClass = TicketStatus.class, allowNull = true)
    private String newStatus;

    @EnumValue(enumClass = TicketDepartment.class, allowNull = true)
    private String newDepartment;

}
