package me.parhamziaei.practice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.LoginRequest;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.entity.TwoFactorSession;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.authenticate.AlreadyLoggedInException;
import me.parhamziaei.practice.exception.custom.authenticate.InvalidTwoFactorException;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TwoFactorService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.CookieBuilder;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestCtrl {

    private final UserService userService;
    private final JwtService jwtService;
    private final TwoFactorService twoFAService;
    private final AuthenticationManager authenticationManager;
    private final MessageService messageService;

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
//        String jwt = jwtService.extractJwtFromRequest(request);
//        String refreshToken = jwtService.extractRefreshTokenFromRequest(request);
//        if (jwt != null && jwtService.isTokenValid(jwt)) {
//            throw new AlreadyLoggedInException(); //todo remember to handle brute force here!
//        }
//
//        UsernamePasswordAuthenticationToken token =
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getEmail(),
//                        loginRequest.getPassword()
//                );
//
//        Authentication auth = authenticationManager.authenticate(token);
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        UserDetails user = userService.loadUserByUsername(loginRequest.getEmail());
//        String newJwt = jwtService.generateToken(user);
//
//        Cookie jwtCookie = new Cookie("JWT", newJwt);
//        jwtCookie.setPath("/");
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setMaxAge(1800);
//        jwtCookie.setSecure(false);
//        jwtCookie.setAttribute("SameSite", "Strict");
//
//        if (loginRequest.isRememberMe()) {
//            String newRefreshToken = jwtService.generateRefreshToken(user);
//            Cookie refreshTokenCookie = new Cookie("REFRESH", newRefreshToken);
//            refreshTokenCookie.setPath("/");
//            refreshTokenCookie.setHttpOnly(true);
//            refreshTokenCookie.setMaxAge(604800);
//            refreshTokenCookie.setSecure(false);
//            refreshTokenCookie.setAttribute("SameSite", "Strict");
//
//            response.addCookie(refreshTokenCookie);
//
//            jwtCookie.setMaxAge(604800);
//        }
//
//        response.addCookie(jwtCookie);
//
//        userService.updateLastLogin(loginRequest.getEmail());
//        return ResponseEntity.ok().body(
//                new ApiResponse(
//                        true,
//                        "LOGIN_SUCCESS",
//                        "Login successful",
//                        null
//                )
//        );
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
//        String jwt = jwtService.extractJwtFromRequest(request);
//        if (jwt != null && jwtService.isTokenValid(jwt)) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body("ALREADY_LOGGED_IN");
//        }
//        try {
//            userService.register(registerRequest);
//            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @PostMapping("/2fa-verify")
    public ResponseEntity<?> twoFactorVerify(@RequestBody String code, HttpServletRequest request, HttpServletResponse response) {
        String jwt = jwtService.extractJwtFromRequest(request);
        String refreshToken = jwtService.extractRefreshTokenFromRequest(request); // why I added this to this controller? :/
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            throw new AlreadyLoggedInException(); //todo remember to handle brute force here!
        }

        String twoFactorJwt = jwtService.extractTwoFactorTokenFromRequest(request);

        if (twoFactorJwt == null) {
            throw new InvalidTwoFactorException();
        }

        TwoFactorSession twoFactorSession = twoFAService.verifyAndGetSession(twoFactorJwt, code);

        if (twoFactorSession != null && twoFactorSession.isVerified()) {
            UserDetails user = userService.loadUserByUsername(jwtService.extractUsername(twoFactorJwt));
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            String newJwt = jwtService.generateToken(user);
            Cookie emptyTwoFactorCookie = CookieBuilder.emptyCookie("2FA");

            Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, 1800);

            response.addCookie(accessTokenCookie);
            response.addCookie(emptyTwoFactorCookie);

            if (twoFactorSession.isRememberMe()) {
                String newRefreshToken = jwtService.generateRefreshToken(user);
                accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, 604000);
                Cookie refreshTokenCookie = CookieBuilder.buildRefreshTokenCookie(newRefreshToken);

                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);
            }

            return ResponseBuilder.buildSuccess(
                    "VERIFIED",
                    messageService.get(Message.AUTH_LOGIN_SUCCESS),
                    HttpStatus.OK
            );
        }

        return ResponseBuilder.buildFailed(
                "DENIED",
                messageService.get(Message.AUTH_TWO_FACTOR_INVALID),
                HttpStatus.BAD_REQUEST
        );
    }

    @Operation(summary = "login operation")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String jwt = jwtService.extractJwtFromRequest(request);
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            throw new AlreadyLoggedInException(); //todo remember to handle brute force here!
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
        );

        if (userService.isUserTwoFAEnabled(auth.getName())) {
            SecureRandom random = new SecureRandom();
            TwoFactorSession twoFactor = TwoFactorSession.builder()
                    .code(String.format("%04d", random.nextInt(10000)))
                    .rememberMe(loginRequest.isRememberMe())
                    .userEmail(loginRequest.getEmail().toLowerCase())
                    .verified(false)
                    .build();

            String sessionId = twoFAService.addTwoFactor(twoFactor);
            String twoFactorToken = jwtService.generateTwoFAToken(loginRequest.getEmail(), sessionId);
            Cookie twoFactorCookie = CookieBuilder.twoFactorCookie(twoFactorToken);

            response.addCookie(twoFactorCookie);

            return ResponseBuilder.buildSuccess(
                    "SENT",
                    messageService.get(Message.AUTH_TWO_FACTOR_SENT),
                    auth.getName(),
                    HttpStatus.OK
            );
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetails user = userService.loadUserByUsername(loginRequest.getEmail());
        String newJwt = jwtService.generateToken(user);

        Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, 1800);

        if (loginRequest.isRememberMe()) {
            String newRefreshToken = jwtService.generateRefreshToken(user);
            Cookie refreshTokenCookie = CookieBuilder.buildRefreshTokenCookie(newRefreshToken);
            accessTokenCookie.setMaxAge(604000);
            response.addCookie(refreshTokenCookie);
        }

        response.addCookie(accessTokenCookie);

        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                messageService.get(Message.AUTH_LOGIN_SUCCESS),
                HttpStatus.OK
        );

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            throw new AlreadyLoggedInException();
        }
        userService.register(registerRequest);
        return ResponseBuilder.buildSuccess(
                "REGISTERED_SUCCESSFULLY",
                "user registration succeed",
                HttpStatus.OK
        );
    }

    @PostMapping("/logout")
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

        return ResponseBuilder.buildSuccess(
                "LOGGED_OUT_SUCCESSFULLY",
                "user successfully logged out",
                HttpStatus.OK
        );
    }

}
