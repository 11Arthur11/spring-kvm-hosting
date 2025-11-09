package me.parhamziaei.practice.controller.global;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.authenticate.TwoFactorRequest;
import me.parhamziaei.practice.entity.redis.TwoFactorSession;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.authenticate.InvalidTwoFactorException;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TwoFactorService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.CookieBuilder;
import me.parhamziaei.practice.util.ResponseBuilder;
import me.parhamziaei.practice.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class TwoFactorController {

    private final TwoFactorService twoFAService;
    private final JwtService jwtService;
    private final UserService userService;
    private final MessageService messageService;

    @Operation(summary = "Validate TwoFactor verification code")
    @PostMapping("/2fa-verify")
    public ResponseEntity<?> twoFactorVerify(@RequestBody TwoFactorRequest twoFactorRequest, HttpServletRequest request, HttpServletResponse response) {
        final String code = twoFactorRequest.getCode();
        final String twoFactorJwt = jwtService.extractTwoFactorTokenFromRequest(request);

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
            String newJwt = jwtService.generateAccessToken(user);

            Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, SecurityUtil.ACCESS_JWT_TTL);

            response.addCookie(CookieBuilder.emptyCookie("2FA"));

            if (twoFactorSession.isRememberMe()) {
                String newRefreshToken = jwtService.generateRefreshToken(user);
                response.addCookie(CookieBuilder.buildRefreshTokenCookie(newRefreshToken));
                accessTokenCookie.setMaxAge((int) SecurityUtil.REFRESH_JWT_TTL.toSeconds());
            }

            response.addCookie(accessTokenCookie);
            userService.updateLastLogin(user.getUsername());

            return ResponseBuilder.buildSuccess(
                    "LOGIN_DONE",
                    messageService.get(Message.AUTH_LOGIN_SUCCESS),
                    HttpStatus.OK
            );
        }

        return ResponseBuilder.buildFailed(
                "DENIED",
                messageService.get(Message.TWO_FACTOR_INVALID),
                HttpStatus.BAD_REQUEST
        );
    }

    @Operation(summary = "Validate email verification code")
    @PostMapping("/email-verify")
    public ResponseEntity<?> verifyEmail(@RequestBody TwoFactorRequest twoFactorRequest, HttpServletRequest request, HttpServletResponse response) {
        final String code = twoFactorRequest.getCode();
        final String twoFactorJwt = jwtService.extractTwoFactorTokenFromRequest(request);
        final String userEmail = jwtService.extractUsername(twoFactorJwt);

        if (twoFactorJwt == null) {
            throw new InvalidTwoFactorException();
        }

        if (twoFAService.isVerificationCodeCorrect(twoFactorJwt, code)) {
            userService.enableUser(userEmail);

            UserDetails user = userService.loadUserByUsername(userEmail);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            final String accessToken = jwtService.generateAccessToken(user);

            response.addCookie(CookieBuilder.buildAccessTokenCookie(accessToken, SecurityUtil.ACCESS_JWT_TTL));
            response.addCookie(CookieBuilder.emptyCookie("2FA"));

            return ResponseBuilder.buildSuccess(
                    "REGISTER_DONE",
                    messageService.get(Message.REGISTER_SUCCESSFULLY_DONE),
                    HttpStatus.OK
            );
        }
        return ResponseBuilder.buildFailed(
                "DENIED",
                messageService.get(Message.TWO_FACTOR_INVALID),
                HttpStatus.BAD_REQUEST
        );
    }

    @Operation(summary = "Resending email verification code if session wasn't expired")
    @PostMapping("/email-verify/resend")
    public ResponseEntity<?> resendEmailVerificationCode(HttpServletRequest request, HttpServletResponse response) {
        final String emailVerifyToken = jwtService.extractTwoFactorTokenFromRequest(request);
        if (
                emailVerifyToken != null
                        && jwtService.isSignatureValid(emailVerifyToken)
                        && !jwtService.isTokenExpired(emailVerifyToken)
        ) {
            if (twoFAService.hasActiveEmailVerifySession(jwtService.extractSessionIdFromTwoFactorToken(emailVerifyToken))) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            Date remainingTime = jwtService.extractExpiration(emailVerifyToken);
            if (remainingTime.toInstant().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES))) {
                remainingTime = Date.from(Instant.now().plus(2, ChronoUnit.MINUTES));
            }
            final String email = jwtService.extractUsername(emailVerifyToken);
            final String sessionId = twoFAService.sendEmailVerificationCode(email);
            final String newEmailVerifyToken = jwtService.generateEmailVerifyToken(email, sessionId, remainingTime);
            response.addCookie(CookieBuilder.twoFactorCookie(newEmailVerifyToken, remainingTime));

            return ResponseBuilder.buildSuccess(
                    "CODE_RESENT",
                    messageService.get(Message.TWO_FACTOR_RESEND),
                    HttpStatus.OK
            );
        } else {
            return ResponseBuilder.buildFailed(
                    "INVALID_SESSION",
                    messageService.get(Message.TWO_FACTOR_RESEND_SESSION_EXPIRED),
                    HttpStatus.GONE
            );
        }
    }

    @Operation(summary = "Resending TwoFactor verification code if session wasn't expired")
    @PostMapping("/2fa-verify/resend")
    public ResponseEntity<?> resendTwoFactorCode(HttpServletRequest request, HttpServletResponse response) {
        final String twoFactorToken = jwtService.extractTwoFactorTokenFromRequest(request);
        if (
                twoFactorToken != null
                        && jwtService.isSignatureValid(twoFactorToken)
                        && !jwtService.isTokenExpired(twoFactorToken)
        ) {
            if (twoFAService.hasActiveTwoFactorSession(jwtService.extractSessionIdFromTwoFactorToken(twoFactorToken))) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            Date remainingTime = jwtService.extractExpiration(twoFactorToken);
            if (remainingTime.toInstant().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES))) {
                remainingTime = Date.from(Instant.now().plus(2, ChronoUnit.MINUTES));
            }
            final String email = jwtService.extractUsername(twoFactorToken);
            final boolean rememberMe = jwtService.extractRememberMeFromTwoFactorToken(twoFactorToken);
            final String sessionId = twoFAService.sendTwoFactor(email, rememberMe);
            final String newTwoFactorToken = jwtService.generateTwoFactorToken(email, sessionId, rememberMe, remainingTime);
            response.addCookie(CookieBuilder.twoFactorCookie(newTwoFactorToken, remainingTime));

            return ResponseBuilder.buildSuccess(
                    "CODE_RESENT",
                    messageService.get(Message.TWO_FACTOR_RESEND),
                    HttpStatus.OK
            );
        } else {
            return ResponseBuilder.buildFailed(
                    "INVALID_SESSION",
                    messageService.get(Message.TWO_FACTOR_RESEND_SESSION_EXPIRED),
                    HttpStatus.GONE
            );
        }
    }

}
