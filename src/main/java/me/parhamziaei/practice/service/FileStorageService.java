package me.parhamziaei.practice.service;

import me.parhamziaei.practice.exception.custom.service.FileStorageServiceException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Path ticketAttachmentsPath;

    public FileStorageService(Environment env) {
        this.fileStorageLocation = Paths.get(env.getProperty("app.file.upload-dir", "./uploads"))
                .toAbsolutePath()
                .normalize();

        this.ticketAttachmentsPath = Paths.get(env.getProperty("app.file.ticket-attachment-dir", "./uploads/ticket-attachments"))
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(ticketAttachmentsPath);
        } catch (IOException ignored) {

        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String[] fileNameParts = fileName.split("\\.");

        return fileNameParts[fileNameParts.length - 1];
    }

    public String storeTicketAttachment(MultipartFile file) {
        UUID uuid = UUID.randomUUID();

        String fileName = new Date().getTime() + "_" + uuid + "." + getFileExtension(file.getOriginalFilename());

        try {
            Path targetLocation = this.ticketAttachmentsPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ignored) {
            throw new FileStorageServiceException("FAILED TO STORING TICKET ATTACHMENT");
        }
    }

}
