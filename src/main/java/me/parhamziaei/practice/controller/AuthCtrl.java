package me.parhamziaei.practice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.service.JwtService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthCtrl {

    private final JwtService jwtService;

    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            return "redirect:/greeting";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

}
