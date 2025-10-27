package me.parhamziaei.practice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.response.ProfileResponse;
import me.parhamziaei.practice.entity.jpa.Role;
import me.parhamziaei.practice.entity.jpa.User;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MainRestCtrl {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/favicon.ico")
    private void noFavicon() {
        System.out.println("favicon.ico is not configured!");
    }

    @GetMapping("/greeting")
    public ResponseEntity<String> greeting() {
        return ResponseEntity.ok("Hello From Backend");
    }

    @GetMapping("/goodbye")
    public ResponseEntity<String> goodbye() {
        return ResponseEntity.ok("Goodbye From Backend");
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> getUserProfile(HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(
                jwtService.extractJwtFromRequest(request)
        );
        User user = (User) userService.loadUserByUsername(userEmail);
        ProfileResponse profileResponse = new ProfileResponse(
                user.getEmail(),
                user.getFullName(),
                user.getWallet().getBalance(),
                user.getCreatedAt(),
                user.getLastLogin(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()),
                user.isLocked()
        );
        return ResponseBuilder.buildSuccess(
                "DATA",
                "",
                profileResponse,
                HttpStatus.OK
        );
    }

}
