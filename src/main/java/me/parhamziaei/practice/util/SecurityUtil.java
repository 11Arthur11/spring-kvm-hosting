package me.parhamziaei.practice.util;

import java.util.Arrays;
import java.util.List;

public class SecurityUtil {

    private SecurityUtil() {}

    public final static List<String> SKIP_URLs = Arrays.asList(
            "/api/v1/auth/**",
            "/docs/**",
            "/swagger-ui/**"
    );

    public static boolean requestMatcher(String requestUrl) {
        for(String filteredUrl : SKIP_URLs) {
            if (filteredUrl.endsWith("/**")) {
                String baseFilteredPath = filteredUrl.substring(0, filteredUrl.length() - 3);
                System.out.println(baseFilteredPath);
                if (requestUrl.startsWith(baseFilteredPath)) {
                    return true;
                }
            }
            if (requestUrl.equalsIgnoreCase(filteredUrl)) {return true;}
        }
        return false;
    }

}
