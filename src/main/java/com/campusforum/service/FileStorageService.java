package com.campusforum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootUploadLocation;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".jpg", ".jpeg", ".png", ".gif", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt"
    );
    private static final List<String> FORBIDDEN_EXTENSIONS = Arrays.asList(
            ".exe", ".bat", ".cmd", ".sh", ".php", ".jsp", ".jar", ".js", ".vbs", ".msi", ".dll"
    );

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.rootUploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootUploadLocation);
            Files.createDirectories(this.rootUploadLocation.resolve("notices"));
            Files.createDirectories(this.rootUploadLocation.resolve("events"));
            Files.createDirectories(this.rootUploadLocation.resolve("documents"));
            Files.createDirectories(this.rootUploadLocation.resolve("profiles"));
            Files.createDirectories(this.rootUploadLocation.resolve("resources"));
        } catch (Exception ex) {
            throw new RuntimeException("Could not initialize upload directories", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        return storeFile(file, "notices");
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed");
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFileName);
        }

        String extension = "";
        int extIndex = originalFileName.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFileName.substring(extIndex).toLowerCase();
        }

        if (FORBIDDEN_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type " + extension + " is strictly prohibited for security reasons.");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type " + extension + ". Allowed: PDF, JPG, PNG, DOC, DOCX, TXT, XLS");
        }

        try {
            Path targetDir = this.rootUploadLocation.resolve(subDirectory).normalize();
            Files.createDirectories(targetDir);

            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Path targetLocation = targetDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    public Path getFilePath(String relativePath) {
        return this.rootUploadLocation.resolve(relativePath).normalize();
    }
}
