package com.ferreteria_edu.ferreteria_api.mapper;

import com.ferreteria_edu.ferreteria_api.dto.productDto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.model.Category;
import com.ferreteria_edu.ferreteria_api.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductMapper {

    public static ProductDTO toDTO(Product p) {

        BigDecimal price = p.getPrice();
        BigDecimal margin = p.getProfitMargin(); // porcentaje

        BigDecimal profit = price
                .multiply(margin)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = price.add(profit);

        // 🔥 redondeo comercial al 100 superior
        BigDecimal roundedFinalPrice = finalPrice
                .divide(BigDecimal.TEN, 0, RoundingMode.CEILING)
                .multiply(BigDecimal.TEN);

        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .img(p.getImg())
                .price(price)
                .profitMargin(margin)
                .profit(profit)
                .finalPrice(roundedFinalPrice) // 👈 ya viene listo
                .stock(p.getStock())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
             .build();
    }

    public static Product toEntity(ProductDTO dto, Category c) {

        Product p = new Product();
        p.setId(dto.getId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setImg(dto.getImg());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setState(dto.isState());
        p.setCategory(c);
        p.setProfitMargin(dto.getProfitMargin());

        return p;
    }
}
