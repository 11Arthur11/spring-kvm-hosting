package me.parhamziaei.practice.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.CookieBuilder;
import me.parhamziaei.practice.util.SecurityUtil;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;
        String userEmail = null;
        String requestURI = request.getRequestURI();

        if (SecurityUtil.requestMatcher(requestURI)) {
            System.out.println("requestURI Bypassed JwtAuthFilter: " + requestURI); // todo remove dev log
            filterChain.doFilter(request, response);
            return;
        }

        jwt = jwtService.extractJwtFromRequest(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null) {
                UserDetails user = userService.loadUserByUsername(userEmail);
                String refreshToken = jwtService.extractRefreshTokenFromRequest(request);

                if (jwtService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JwtFilter Success for user: " + userEmail + " requestURI: " + requestURI); // todo remove dev log
                } else if (refreshToken != null && jwtService.isRefreshTokenValid(refreshToken, user)) {
                    String newRefreshedJwt = jwtService.generateAccessToken(user, Duration.ofMinutes(30));

                    Cookie jwtCookie = CookieBuilder.buildAccessTokenCookie(newRefreshedJwt, 1800);

                    response.addCookie(jwtCookie);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JwtFilter Refresh Token Success for user: " + userEmail + " requestURI: " + requestURI); // todo remove dev log
                }
            }
        }

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            System.out.println("Access denied in jwtAuthFilter to requestURI: " + requestURI); // todo remove dev log
            return;
        }

        filterChain.doFilter(request, response);
    }

}
