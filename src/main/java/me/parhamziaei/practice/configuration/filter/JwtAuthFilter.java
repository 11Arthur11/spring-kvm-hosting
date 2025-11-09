package me.parhamziaei.practice.configuration.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        String refreshToken = null;
        String userEmail = null;
        String requestURI = request.getRequestURI();

        if (SecurityUtil.requestMatcher(requestURI)) {
            log.debug("requestURI Bypassed JwtAuthFilter: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        jwt = jwtService.extractJwtFromRequest(request);
        refreshToken = jwtService.extractRefreshTokenFromRequest(request);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwt != null) {
                userEmail = jwtService.extractUsername(jwt);
            }
            if (userEmail == null) {
                userEmail = jwtService.extractUsername(refreshToken);
            }
            if (userEmail != null) {
                UserDetails user = userService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JwtFilter Success for user: {} requestURI: {}", userEmail, requestURI);
                } else if (refreshToken != null && jwtService.isRefreshTokenValid(refreshToken, user)) {
                    String newRefreshedJwt = jwtService.generateAccessToken(user);
                    response.addCookie(CookieBuilder.buildAccessTokenCookie(newRefreshedJwt, SecurityUtil.ACCESS_JWT_TTL));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JwtFilter Refresh Token Success for user: {} requestURI: {}", userEmail, requestURI);
                }
            }
        }

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            log.info("JwtFilter Unauthorized for user: {} requestURI: {}", userEmail, requestURI);
            return;
        }

        filterChain.doFilter(request, response);
    }

}
