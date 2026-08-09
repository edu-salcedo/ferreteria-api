package com.ferreteria_edu.ferreteria_api.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ferreteria_edu.ferreteria_api.order.dto.ImportSalesResultDTO;
import com.ferreteria_edu.ferreteria_api.order.service.SalesExcelService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("sales")
@RequiredArgsConstructor
public class SalesImportController {
    private final SalesExcelService salesExcelService;

    @PostMapping("/import")
    public ResponseEntity<ImportSalesResultDTO> importExcel(
            @RequestParam("file") MultipartFile file) throws Exception {

        return ResponseEntity.ok(
                salesExcelService.importExcel(file));
    }

}
