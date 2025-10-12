package me.parhamziaei.practice.util;

import jakarta.servlet.http.Cookie;

public class CookieBuilder {

    private CookieBuilder() {}

    // todo make this true in production phase
    private static final boolean SECURE_COOKIE_ENABLED = false;

    public static Cookie twoFactorCookie(String token) {
        Cookie cookie = new Cookie("2FA", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(120);
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

    public static Cookie buildAccessTokenCookie(String token, int expiresInSeconds) {
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(expiresInSeconds);
        jwtCookie.setSecure(SECURE_COOKIE_ENABLED);
        jwtCookie.setAttribute("SameSite", "Strict");
        return jwtCookie;
    }

    public static Cookie buildRefreshTokenCookie(String token) {
        Cookie refreshTokenCookie = new Cookie("REFRESH", token);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(604800);
        refreshTokenCookie.setSecure(SECURE_COOKIE_ENABLED);
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        return refreshTokenCookie;
    }

}
