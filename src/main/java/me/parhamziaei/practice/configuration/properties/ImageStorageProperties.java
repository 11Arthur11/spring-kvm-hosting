package me.parhamziaei.practice.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app.file.media-properties")
public record ImageStorageProperties(
        String ticketAttachmentsPath,
        Integer maximumMediaSizeMb,
        Set<String> allowedMediaMimeType
) {}
