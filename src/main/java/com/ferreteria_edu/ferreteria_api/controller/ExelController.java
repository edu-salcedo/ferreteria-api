package com.ferreteria_edu.ferreteria_api.controller;

import com.ferreteria_edu.ferreteria_api.service.ExcelService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("excel")
public class ExelController {

     private final ExcelService excelService;

    @PostMapping("/import")
    public ResponseEntity<?> ProductImport(@RequestParam("file") MultipartFile file) {
        try {
            excelService.ExcelImport(file);
            return ResponseEntity.ok("Productos importados correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al importar: " + e.getMessage());
        }
    }
}
