package me.parhamziaei.practice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.LoginRequest;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthRestCtrl {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String jwt = jwtService.extractJwtFromRequest(request);
        String refreshToken = jwtService.extractRefreshTokenFromRequest(request);
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("ALREADY_LOGGED_IN");
        }
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    );
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails user = userService.loadUserByUsername(loginRequest.getEmail());
            String newJwt = jwtService.generateToken(user);

            Cookie jwtCookie = new Cookie("JWT", newJwt);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(1800);
            jwtCookie.setSecure(false);
            jwtCookie.setAttribute("SameSite", "Strict");



            if (loginRequest.isRememberMe()) {
                String newRefreshToken = jwtService.generateRefreshToken(user);
                Cookie refreshTokenCookie = new Cookie("REFRESH", newRefreshToken);
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setMaxAge(604800);
                refreshTokenCookie.setSecure(false);
                refreshTokenCookie.setAttribute("SameSite", "Strict");

                response.addCookie(refreshTokenCookie);

                jwtCookie.setMaxAge(604800);
            }

            response.addCookie(jwtCookie);

            userService.updateLastLogin(loginRequest.getEmail());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body("ACCOUNT_DISABLED");
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("INVALID_CREDENTIALS");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("INVALID_EMAIL");
        } catch (LockedException e) {
            return ResponseEntity.badRequest().body("ACCOUNT_LOCKED");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("ALREADY_LOGGED_IN");
        }
        try {
            userService.register(registerRequest);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/logout") // todo make this post mapping
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        String refreshToken = jwtService.extractRefreshTokenFromRequest(request);

        Cookie replaceJwtCookie = new Cookie("JWT", "");
        replaceJwtCookie.setHttpOnly(true);
        replaceJwtCookie.setMaxAge(0);
        replaceJwtCookie.setSecure(false);
        replaceJwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(replaceJwtCookie);

        Cookie replaceRefreshTokenCookie = new Cookie("REFRESH", "");
        replaceRefreshTokenCookie.setHttpOnly(true);
        replaceRefreshTokenCookie.setMaxAge(0);
        replaceRefreshTokenCookie.setSecure(false);
        replaceRefreshTokenCookie.setAttribute("SameSite", "Strict");
        response.addCookie(replaceRefreshTokenCookie);

        if (refreshToken != null) {
            jwtService.deActivateRefreshToken(refreshToken);
        }

        return ResponseEntity.ok().body("LOGGED_OUT");
    }

}
