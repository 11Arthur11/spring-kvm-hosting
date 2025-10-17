package me.parhamziaei.practice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.LoginRequest;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.entity.redis.TwoFactorSession;
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

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestCtrl {

    private final UserService userService;
    private final JwtService jwtService;
    private final TwoFactorService twoFAService;
    private final AuthenticationManager authenticationManager;
    private final MessageService messageService;

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
            String newJwt = jwtService.generateAccessToken(user, Duration.ofMinutes(30));

            Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, 1800);

            response.addCookie(CookieBuilder.emptyCookie("2FA"));

            if (twoFactorSession.isRememberMe()) {
                String newRefreshToken = jwtService.generateRefreshToken(user, Duration.ofDays(7));
                Cookie refreshTokenCookie = CookieBuilder.buildRefreshTokenCookie(newRefreshToken);
                accessTokenCookie.setMaxAge(604000);
                response.addCookie(refreshTokenCookie);
            }

            response.addCookie(accessTokenCookie);

            return ResponseBuilder.buildSuccess(
                    "DONE",
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
                         "SENT",
                         messageService.get(Message.REGISTER_ACCOUNT_NOT_VERIFIED),
                         HttpStatus.BAD_REQUEST
                 );
             }

             final String sessionId = twoFAService.sendEmailVerificationCode(email);
             final String emailVerifyJwt = jwtService.generateTwoFactorToken(email, sessionId, Duration.ofMinutes(5));
             response.addCookie(CookieBuilder.twoFactorCookie(emailVerifyJwt, Duration.ofMinutes(5)));

             return ResponseBuilder.buildFailed(
                     "SENT",
                     messageService.get(Message.REGISTER_ACCOUNT_NOT_VERIFIED),
                     HttpStatus.BAD_REQUEST
             );
         }

        if (userService.isUserTwoFAEnabled(auth.getName())) {
            String sessionId = twoFAService.sendTwoFactor(email, loginRequest.isRememberMe());
            String twoFactorToken = jwtService.generateTwoFactorToken(email, sessionId, Duration.ofMinutes(2));
            Cookie twoFactorCookie = CookieBuilder.twoFactorCookie(twoFactorToken, Duration.ofMinutes(2));

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
        String newJwt = jwtService.generateAccessToken(user, Duration.ofMinutes(30));

        Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(newJwt, 1800);

        if (loginRequest.isRememberMe()) {
            String newRefreshToken = jwtService.generateRefreshToken(user, Duration.ofDays(7));
            Cookie refreshTokenCookie = CookieBuilder.buildRefreshTokenCookie(newRefreshToken);
            accessTokenCookie.setMaxAge(604000);
            response.addCookie(refreshTokenCookie);
        }

        response.addCookie(accessTokenCookie);

        return ResponseBuilder.buildSuccess(
                "DONE",
                messageService.get(Message.AUTH_LOGIN_SUCCESS),
                HttpStatus.OK
        );

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request, HttpServletResponse response) {
        String jwt = jwtService.extractJwtFromRequest(request);
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            throw new AlreadyLoggedInException();
        }

        userService.register(registerRequest);
        final String email = registerRequest.getEmail().toLowerCase().trim();
        final String sessionId = twoFAService.sendEmailVerificationCode(email);
        final String twoFactorToken = jwtService.generateTwoFactorToken(email, sessionId, Duration.ofMinutes(5));
        final Cookie verificationCookie = CookieBuilder.twoFactorCookie(twoFactorToken, Duration.ofMinutes(5));
        response.addCookie(verificationCookie);

        return ResponseBuilder.buildSuccess(
                "SENT",
                messageService.get(Message.REGISTER_SUCCESS_WAITING_FOR_ACTIVATION),
                HttpStatus.OK
        );
    }

    @PostMapping("/email-verify")
    public ResponseEntity<?> verifyEmail(@RequestBody String code, HttpServletRequest request, HttpServletResponse response) {
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

            final String accessToken = jwtService.generateAccessToken(user, Duration.ofMinutes(30));
            final Cookie accessTokenCookie = CookieBuilder.buildAccessTokenCookie(accessToken, 1800);

            response.addCookie(accessTokenCookie);
            response.addCookie(CookieBuilder.emptyCookie("2FA"));

            return ResponseBuilder.buildSuccess(
                    "DONE",
                    messageService.get(Message.REGISTER_SUCCESSFULLY_DONE), // todo make language enum var
                    HttpStatus.OK
            );
        }

        return ResponseBuilder.buildFailed(
                "DENIED",
                null, // todo make language enum var
                HttpStatus.BAD_REQUEST
        );

    }

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
                "DONE",
                messageService.get(Message.AUTH_LOGOUT_SUCCESS),
                HttpStatus.OK
        );
    }

}
