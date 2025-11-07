package me.parhamziaei.practice.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SecurityUtil {

    private SecurityUtil() {}

    public final static List<String> SKIP_URLs = Arrays.asList(
            "/api/v1/auth/**",
            "/docs/**",
            "/swagger-ui/**"
    );

    public final static Duration TWO_FACTOR_COOKIE_EXPIRE = Duration.ofSeconds(1800);
    public final static Duration TWO_FACTOR_TOKEN_TTL = Duration.ofSeconds(1800);

    public final static Duration REFRESH_JWT_TTL = Duration.ofSeconds(604800);
    public final static Duration ACCESS_JWT_TTL = Duration.ofSeconds(1800);
    public final static Duration FORGOT_PASSWORD_JWT_TTL = Duration.ofSeconds(900);
    public final static Duration EMAIL_VERIFY_SESSION_TTL= Duration.ofSeconds(300);
    public final static Duration TWO_FACTOR_SESSION_TTL = Duration.ofSeconds(120);
    public final static Duration FORGOT_PASSWORD_SESSION_TTL = Duration.ofSeconds(180);

    public static boolean requestMatcher(String requestUrl) {
        for(String filteredUrl : SKIP_URLs) {
            if (filteredUrl.endsWith("/**")) {
                String baseFilteredPath = filteredUrl.substring(0, filteredUrl.length() - 3);
                if (requestUrl.startsWith(baseFilteredPath)) {
                    return true;
                }
            }
            if (requestUrl.equalsIgnoreCase(filteredUrl)) {return true;}
        }
        return false;
    }

}
