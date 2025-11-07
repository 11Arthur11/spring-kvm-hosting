package me.parhamziaei.practice.dto.request.authenticate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Data
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "EMAIL_EMPTY")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "PASSWORD_EMPTY")
    @Length(min = 8, message = "PASSWORD_TOO_SHORT")
    private String password;

    private boolean rememberMe;

}
