package com.ferreteria_edu.ferreteria_api.purchase.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequestDTO {

    private Long supplierId;
    private List<PurchaseItemDTO> items;
}

