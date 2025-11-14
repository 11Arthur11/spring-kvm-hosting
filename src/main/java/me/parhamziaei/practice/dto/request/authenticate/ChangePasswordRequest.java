package me.parhamziaei.practice.dto.request.authenticate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import me.parhamziaei.practice.validation.annotation.PasswordValidation;
import org.hibernate.validator.constraints.Length;

@Data
@Getter
public class ChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @PasswordValidation
    private String newPassword;

    @NotBlank(message = "PASSWORD_CONFIRM_EMPTY")
    private String newPasswordConfirm;

}
