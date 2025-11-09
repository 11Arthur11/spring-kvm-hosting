package me.parhamziaei.practice.controller.global;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.dto.request.authenticate.ForgotPasswordRequest;
import me.parhamziaei.practice.dto.request.authenticate.LoginRequest;
import me.parhamziaei.practice.dto.request.authenticate.RegisterRequest;
import me.parhamziaei.practice.entity.redis.ForgotPasswordSession;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TwoFactorService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.CookieBuilder;
import me.parhamziaei.practice.util.ResponseBuilder;
import me.parhamziaei.practice.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticateController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TwoFactorService twoFAService;
    private final AuthenticationManager authenticationManager;
    private final MessageService messageService;

    @Operation(summary = "Validating user auth information")
    @RequestMapping(value = "/validate", method = RequestMethod.HEAD)
    public ResponseEntity<?> validateLogin(HttpServletRequest request) {
        final String jwt = jwtService.extractJwtFromRequest(request);
        if (jwtService.isTokenValid(jwt) && SecurityContextHolder.getContext().getAuthentication() != null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Login entry")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        final String email = loginRequest.getEmail().toLowerCase().trim();

         try {
             auth = authenticationManager.authenticate(
                     new UsernamePasswordAuthenticationToken(
                             email,
                             loginRequest.getPassword()
                     )
             );
         } catch (DisabledException ignored) {
             final String twoFactorToken = jwtService.extractTwoFactorTokenFromRequest(request);

             if (
                     twoFactorToken != null &&
                     twoFAService.hasActiveEmailVerifySession(
                             jwtService.extractSessionIdFromTwoFactorToken(twoFactorToken
                     ))
             ) {
                 return ResponseBuilder.buildFailed(
                         "EMAIL_VERIFY_SENT",
                         messageService.get(Message.REGISTER_ACCOUNT_NOT_VERIFIED),
                         HttpStatus.BAD_REQUEST
                 );
             }

             final String sessionId = twoFAService.sendEmailVerificationCode(email);
             final String emailVerifyJwt = jwtService.generateEmailVerifyToken(email, sessionId);
             response.addCookie(CookieBuilder.twoFactorCookie(emailVerifyJwt));

             return ResponseBuilder.buildFailed(
                     "EMAIL_VERIFY_SENT",
                     messageService.get(Message.REGISTER_ACCOUNT_NOT_VERIFIED),
                     HttpStatus.BAD_REQUEST
             );
         }

        if (userService.isUserTwoFAEnabled(auth.getName())) { // todo extract userService.isUserTwoFAEnabled(auth.getName()) to UserSettingService class
            String sessionId = twoFAService.sendTwoFactor(email, loginRequest.isRememberMe());
            String twoFactorToken = jwtService.generateTwoFactorToken(email, sessionId, loginRequest.isRememberMe());
            Cookie twoFactorCookie = CookieBuilder.twoFactorCookie(twoFactorToken);

            response.addCookie(twoFactorCookie);

            return ResponseBuilder.buildSuccess(
                    "CODE_SENT",
                    messageService.get(Message.TWO_FACTOR_SENT),
                    auth.getName(),
                    HttpStatus.OK
            );
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetails user = userService.loadUserByUsername(loginRequest.getEmail());
        String newJwt = jwtService.generateAccessToken(user);

        Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, SecurityUtil.ACCESS_JWT_TTL);

        if (loginRequest.isRememberMe()) {
            String newRefreshToken = jwtService.generateRefreshToken(user);
            accessTokenCookie.setMaxAge((int) SecurityUtil.REFRESH_JWT_TTL.toSeconds());
            response.addCookie(CookieBuilder.buildRefreshTokenCookie(newRefreshToken));
        }

        response.addCookie(accessTokenCookie);
        userService.updateLastLogin(auth.getName());

        return ResponseBuilder.buildSuccess(
                "LOGIN_DONE",
                messageService.get(Message.AUTH_LOGIN_SUCCESS),
                HttpStatus.OK
        );

    }

    @Operation(summary = "Register entry")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        userService.register(registerRequest);
        final String email = registerRequest.getEmail().toLowerCase().trim();
        final String sessionId = twoFAService.sendEmailVerificationCode(email);
        final String twoFactorToken = jwtService.generateEmailVerifyToken(email, sessionId);
        response.addCookie(CookieBuilder.twoFactorCookie(twoFactorToken));

        return ResponseBuilder.buildSuccess(
                "CODE_SENT",
                messageService.get(Message.TWO_FACTOR_EMAIL_VERIFY_SEND),
                email,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Logout operation and clear JWTs from client")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        String refreshToken = jwtService.extractRefreshTokenFromRequest(request);

        Cookie replaceJwtCookie = CookieBuilder.emptyCookie("JWT");
        Cookie replaceRefreshTokenCookie = CookieBuilder.emptyCookie("REFRESH");

        response.addCookie(replaceJwtCookie);
        response.addCookie(replaceRefreshTokenCookie);

        if (refreshToken != null) {
            jwtService.deActivateRefreshToken(refreshToken);
        }

        return ResponseBuilder.buildSuccess(
                "LOGOUT_DONE",
                messageService.get(Message.AUTH_LOGOUT_SUCCESS),
                HttpStatus.OK
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
        final String email = body.get("email");
        userService.loadUserByUsername(email);
        String twoFactorToken = jwtService.extractTwoFactorTokenFromRequest(request);
        if (
                twoFactorToken != null
                && twoFAService.hasActiveForgotPasswordSession(jwtService.extractSessionIdFromTwoFactorToken(twoFactorToken))
        ) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        final String sessionId = twoFAService.sendForgotPasswordCode(email);
        final String sessionJwt = jwtService.generateForgotPasswordToken(email, sessionId);
        response.addCookie(CookieBuilder.twoFactorCookie(sessionJwt));
        return ResponseBuilder.buildSuccess(
                "CODE_SENT",
                messageService.get(Message.TWO_FACTOR_FORGOT_PASSWORD_CODE_SEND),
                HttpStatus.OK
        );
    }

    @PostMapping("/forgot-password/change")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ForgotPasswordRequest fpRequest, HttpServletRequest request, HttpServletResponse response) {
        final String twoFactorToken = jwtService.extractTwoFactorTokenFromRequest(request);
        ForgotPasswordSession session = twoFAService.validateAndGetForgotPasswordSession(twoFactorToken, fpRequest.getCode());
        userService.changeForgottenPassword(fpRequest, session);
        response.addCookie(CookieBuilder.emptyCookie("2FA"));
        return ResponseBuilder.buildSuccess(
                "DONE",
                messageService.get(Message.USER_PASSWORD_CHANGE_SUCCESS),
                HttpStatus.OK
        );
    }

}