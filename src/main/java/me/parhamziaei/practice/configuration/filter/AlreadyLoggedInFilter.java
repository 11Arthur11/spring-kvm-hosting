package me.parhamziaei.practice.configuration.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class AlreadyLoggedInFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (
                requestURI.startsWith("/api/v1/auth")
                && !requestURI.endsWith("/logout")
                && !requestURI.endsWith("/validate")
        ) {
            String jwt = jwtService.extractJwtFromRequest(request);
            if (jwt != null && jwtService.isTokenValid(jwt)) {
                writeAlreadyLoggedInResponse(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeAlreadyLoggedInResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("type", "ERROR");
        body.put("message", messageService.get(Message.AUTH_ALREADY_LOGGED_IN));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
