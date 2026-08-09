package com.ferreteria_edu.ferreteria_api.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

    private int newProducts;
    private int updatedProducts;

    private int newVariants;
    private int updatedVariants;

    private int newCategories;
}