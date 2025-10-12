package me.parhamziaei.practice.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final JwtService jwtService;

    public boolean isUserLoggedIn(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = false;
        if (auth != null && auth.isAuthenticated() && (!(auth instanceof AnonymousAuthenticationToken)) && request.getCookies() != null) {
            Optional<Cookie> cookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                    .filter(c -> c.getName().equals("JWT"))
                    .findFirst();
            isLoggedIn = cookie.isPresent() && jwtService.isTokenValid(cookie.get().getValue());
        }
        System.out.println(isLoggedIn);
        return isLoggedIn;
    }

    public String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UserDetails) {
            return ((UserDetails) auth).getUsername();
        }
        return null;
    }

}
