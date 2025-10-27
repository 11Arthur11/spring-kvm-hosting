package me.parhamziaei.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Data
@Getter
@Setter
@Builder
public class TicketRequest {

    @NotBlank
    @Length(min = 5)
    private String subject;

    @NotBlank
    private String department;

    private Long serviceId;

    private String submitterEmail;

    private String submitterFullName;

    @NotBlank
    @Length(min = 10)
    private String message;

}
