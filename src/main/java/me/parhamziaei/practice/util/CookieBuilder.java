package me.parhamziaei.practice.util;

import jakarta.servlet.http.Cookie;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class CookieBuilder {

    private CookieBuilder() {}

    // todo make this true in production phase
    private static final boolean SECURE_COOKIE_ENABLED = false;
    private static final Duration TWO_FACTOR_COOKIE_EXPIRE = Duration.ofSeconds(3600);

    public static Cookie twoFactorCookie(String token) {
        Cookie cookie = new Cookie("2FA", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) TWO_FACTOR_COOKIE_EXPIRE.toSeconds());
        cookie.setSecure(SECURE_COOKIE_ENABLED);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static Cookie twoFactorCookie(String token, Date expires) {
        Cookie cookie = new Cookie("2FA", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int)(expires.getTime() - System.currentTimeMillis()) / 1000);
        cookie.setSecure(SECURE_COOKIE_ENABLED);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static Cookie emptyCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(SECURE_COOKIE_ENABLED);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static Cookie buildAccessTokenCookie(String token, Duration ttl) {
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge((int) ttl.toSeconds());
        jwtCookie.setSecure(SECURE_COOKIE_ENABLED);
        jwtCookie.setAttribute("SameSite", "Strict");
        return jwtCookie;
    }

    public static Cookie buildRefreshTokenCookie(String token) {
        Cookie refreshTokenCookie = new Cookie("REFRESH", token);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge((int) SecurityUtil.REFRESH_JWT_TTL.toSeconds());
        refreshTokenCookie.setSecure(SECURE_COOKIE_ENABLED);
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        return refreshTokenCookie;
    }

    public static long getRemainingSeconds(long exp) {
        long currentEpochSeconds = Instant.now().getEpochSecond();
        long remaining = exp - currentEpochSeconds;
        return Math.max(remaining, 0);
    }

}
