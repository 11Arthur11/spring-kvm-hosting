package me.parhamziaei.practice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.ChangePasswordRequest;
import me.parhamziaei.practice.entity.User;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserRestCtrl {

    private final UserService userService;
    private final JwtService jwtService;
    private final MessageService messageService;

    @Operation(summary = "User change password with old pass and new pass, possible responses: 2")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest cpRequest, HttpServletRequest request) {

        final String accessToken = jwtService.extractJwtFromRequest(request);
        final String email = jwtService.extractUsername(accessToken);

        cpRequest.setUserEmail(email);
        final boolean passwordChanged = userService.changePasswordAndGetResult(cpRequest);

        if (passwordChanged) {
            return ResponseBuilder.buildSuccess(
                    "DONE",
                    messageService.get(Message.USER_PASSWORD_CHANGE_SUCCESS),
                    HttpStatus.OK
            );
        }

        return ResponseBuilder.buildFailed(
                "FAILED",
                messageService.get(Message.USER_PASSWORD_CHANGE_FAILED),
                HttpStatus.BAD_REQUEST
        );
    }

}
