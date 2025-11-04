package me.parhamziaei.practice.service;

import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.configuration.properties.ImageStorageProperties;
import me.parhamziaei.practice.entity.jpa.TicketMessageAttachment;
import me.parhamziaei.practice.exception.custom.service.FileStorageServiceException;
import me.parhamziaei.practice.exception.custom.service.ImageTooLargeException;
import me.parhamziaei.practice.exception.custom.service.ImageTypeNotAllowedException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileStorageService {

    private final ImageStorageProperties imageProperties;
    private final Path imagePath;
    private final Set<String> allowedImageExtensions;
    private final Set<String> allowedImageMimeType;

    public FileStorageService(ImageStorageProperties imageProperties) {
        this.imageProperties = imageProperties;
        this.imagePath = Paths.get(imageProperties.ticketAttachmentsPath());
        this.allowedImageExtensions = imageProperties.allowedMimeType().stream()
                .filter(m -> m.contains("/"))
                .map(m -> m.substring(m.indexOf("/") + 1))
                .collect(Collectors.toSet());
        this.allowedImageMimeType = imageProperties.allowedMimeType();
        try {
            System.out.println(imagePath.toAbsolutePath());
            Files.createDirectories(imagePath);
        } catch (IOException e) {
            log.error("Could not create storage directories {}", e.getMessage());
        }
    }

    public String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String[] fileNameParts = fileName.split("\\.");

        return fileNameParts[fileNameParts.length - 1];
    }

    public String getMimeType(MultipartFile fileName) {
        return fileName.getContentType();
    }

    private boolean isImageSizeAllowed(MultipartFile file) {
        double allowedSizeByte = imageProperties.maximumImageSizeMb() * 1024.0 * 1024.0;
        return file.getSize() < allowedSizeByte;
    }

    private boolean isImageTypeAllowed(MultipartFile file) {
        String mimeType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        return allowedImageExtensions.contains(extension) && allowedImageMimeType.contains(mimeType);
    }

    public String storeTicketAttachment(MultipartFile file) {
        if (!isImageSizeAllowed(file)) {
            throw new ImageTooLargeException();
        }
        if (!isImageTypeAllowed(file)) {
            throw new ImageTypeNotAllowedException();
        }

        LocalDate today = LocalDate.now();
        Path datePath = imagePath.resolve(
                today.getYear() + File.separator +
                      String.format("%02d", today.getMonthValue()) + File.separator +
                      String.format("%02d", today.getDayOfMonth())
        );

        UUID uuid = UUID.randomUUID();
        String ext = getFileExtension(file.getOriginalFilename());
        String fileName = new Date().getTime() + "_" + uuid + "." + ext;

        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(datePath);
            Path targetLocation = datePath.resolve(fileName);
            OutputStream outputStream = Files.newOutputStream(targetLocation);
            StreamUtils.copy(inputStream, outputStream);
            return targetLocation.toString();
        } catch (IOException e) {
            log.error("Could not store ticket attachment file: {}", file.getOriginalFilename(), e);
        }
        throw new FileStorageServiceException("Could not store ticket attachment file: " + file.getOriginalFilename());
    }

    public Optional<Resource> loadTicketAttachment(TicketMessageAttachment attachment) {
        Path filePath = Paths.get(attachment.getStoredPath());
        Resource resource = new FileSystemResource(filePath.toFile());
        if (resource.exists() && resource.isReadable()) {
            return Optional.of(resource);
        } else {
            log.error("Ticket attachment file {} does not exist or is not readable", attachment.getStoredPath());
            throw new FileStorageServiceException("Could not load ticket attachment file: " + attachment.getStoredName());
        }
    }

}
