package me.parhamziaei.practice.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TicketFilterRequest extends AbstractPaginationRequest {
    protected String status = "all";
    protected String department = "all";
    protected String sortedBy = "createdAt";
}


