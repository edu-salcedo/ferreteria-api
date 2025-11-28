package com.ferreteria_edu.ferreteria_api.dto.productDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private String img;
    private float price;
    private int stock;
    private boolean state;
    private Integer categoriaId;
    private String categoriaName;
}

