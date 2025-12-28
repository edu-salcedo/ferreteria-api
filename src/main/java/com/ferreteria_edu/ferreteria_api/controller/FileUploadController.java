package com.ferreteria_edu.ferreteria_api.controller;

import com.ferreteria_edu.ferreteria_api.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/files")
public class FileUploadController {


     private final FileStorageService fileStorageService;

    // Endpoint para cargar archivos
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Guardamos el archivo usando el servicio
            String filePath = fileStorageService.save(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(filePath);
        } catch (RuntimeException e) {
            // En caso de error, devolvemos un mensaje adecuado
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
