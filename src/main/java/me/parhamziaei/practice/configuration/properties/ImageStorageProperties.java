package me.parhamziaei.practice.configuration.properties;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.stereotype.Component;

import java.util.Set;

@ConfigurationProperties(prefix = "app.file.image-properties")
public record ImageStorageProperties(
        String ticketAttachmentsPath,
        Integer maximumImageSizeMb,
        Set<String> allowedMimeType
) {}
