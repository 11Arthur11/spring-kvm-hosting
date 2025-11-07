package me.parhamziaei.practice.dto.request.authenticate;

import jakarta.validation.constraints.Email;
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
public class RegisterRequest {

    @NotBlank(message = "EMAIL_EMPTY")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "FULLNAME_EMPTY")
    @Length(min = 5, max = 40, message = "FULLNAME_TOO_SHORT")
    private String fullName;

    @NotBlank(message = "PASSWORD_EMPTY")
    @Length(min = 8, message = "PASSWORD_TOO_SHORT")
    private String rawPassword;

    @NotBlank(message = "PASSWORD_CONFIRM_EMPTY")
    private String rawPasswordConfirm;

}
