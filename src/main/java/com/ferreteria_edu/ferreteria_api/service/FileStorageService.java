package com.ferreteria_edu.ferreteria_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    // Maximum file size (example: 10 MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed file extensions (e.g., images)
    private static final String[] ALLOWED_EXTENSIONS = { "jpg", "jpeg", "png", "gif" };

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir);
    }

    public String save(MultipartFile file) {
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds the maximum allowed size of 10MB");
        }

        // Check file type/extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!isValidExtension(extension)) {
            throw new RuntimeException("archivo invalido. solo archivo de imagenes.");
        }

        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root); // Ensure the directory exists
            }

            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = root.resolve(filename);

            // Copy the file to the target location
            Files.copy(file.getInputStream(), filePath);

            // Return the relative path to the file, to be stored in the DB or returned to the client
            return "/productimg/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar esta imagen", e);
        }
    }

    private String getFileExtension(String filename) {

         return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isValidExtension(String extension) {
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if (allowedExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
