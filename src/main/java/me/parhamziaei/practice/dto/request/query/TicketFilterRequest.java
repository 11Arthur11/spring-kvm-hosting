package me.parhamziaei.practice.dto.request.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import me.parhamziaei.practice.validation.annotation.EnumValue;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TicketFilterRequest extends AbstractPaginationRequest {

    @EnumValue(enumClass = TicketStatus.class, allowNull = true)
    protected String status;

    @EnumValue(enumClass = TicketDepartment.class, allowNull = true)
    protected String department;

    protected String sortedBy = "createdAt";
}


