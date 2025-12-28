package com.ferreteria_edu.ferreteria_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria_edu.ferreteria_api.dto.productDto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.model.Product;
import com.ferreteria_edu.ferreteria_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;


    @GetMapping
    public ResponseEntity<List<ProductDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDTO create(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws JsonProcessingException {
        ProductDTO dto = new ObjectMapper().readValue(productJson, ProductDTO.class);
        return service.create(dto, imageFile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDTO update(
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile

    ) throws JsonProcessingException {

        ProductDTO dto = new ObjectMapper().readValue(productJson, ProductDTO.class);
        System.out.println("Received product JSON: " + productJson);
        if (imageFile != null) {
            System.out.println("Received image: " + imageFile.getOriginalFilename());
        }
        return service.update(id, dto, imageFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(@RequestPart("file") MultipartFile file) {
        try {
            service.processPdf(file);
            return ResponseEntity.ok("PDF procesado correctamente y productos actualizados");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al procesar PDF: " + e.getMessage());
        }
    }


    /** @GetMapping("/category/{categoria}")
     public ResponseEntity<List<ProductDTO>> findByCategory(@PathVariable String categoria) {
         return ResponseEntity.ok(service.findByCategory(categoria));
     }**/
}
