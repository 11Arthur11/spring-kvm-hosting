package me.parhamziaei.practice.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runtime.init")
public record RuntimeInitProperties(
        String adminFullName,
        String adminEmail,
        String adminPassword
) {
}
