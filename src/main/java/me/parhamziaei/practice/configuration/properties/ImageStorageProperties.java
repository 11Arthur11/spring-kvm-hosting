package me.parhamziaei.practice.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.file.media-properties")
public record ImageStorageProperties(
        String ticketAttachmentsPath,
        Integer maximumMediaSizeMb,
        List<String> allowedMediaMimeType,
        List<String> allowedMediaExtension
) {}
