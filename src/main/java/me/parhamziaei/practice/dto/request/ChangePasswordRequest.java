package me.parhamziaei.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Data
@Getter
public class ChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank(message = "PASSWORD_EMPTY")
    @Length(min = 8, message = "PASSWORD_TOO_SHORT")
    private String newPassword;

    @NotBlank(message = "PASSWORD_CONFIRM_EMPTY")
    private String newPasswordConfirm;

}
