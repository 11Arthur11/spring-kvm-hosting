package me.parhamziaei.practice.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "runtime.init")
public record RuntimeInitProperties(
        String adminFullName,
        String adminEmail,
        String adminPassword
) {
}
