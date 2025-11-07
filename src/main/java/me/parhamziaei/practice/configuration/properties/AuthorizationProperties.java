package me.parhamziaei.practice.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.authorization")
public record AuthorizationProperties(

) {
}
