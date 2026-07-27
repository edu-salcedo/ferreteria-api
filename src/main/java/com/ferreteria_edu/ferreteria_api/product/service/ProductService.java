package com.ferreteria_edu.ferreteria_api.product.service;

import com.ferreteria_edu.ferreteria_api.order.service.PriceCalculatorService;
import com.ferreteria_edu.ferreteria_api.product.dto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.product.dto.ProductVariantDTO;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import com.ferreteria_edu.ferreteria_api.product.mapper.ProductMapper;
import com.ferreteria_edu.ferreteria_api.category.entity.Category;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import com.ferreteria_edu.ferreteria_api.category.repository.CategoryRepository;
import com.ferreteria_edu.ferreteria_api.product.mapper.ProductVariantMapper;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductVariantRepository;
import com.ferreteria_edu.ferreteria_api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ProductVariantRepository productVariantRepository;
    private final PriceCalculatorService priceCalculatorService;

    public ProductDTO create(ProductDTO dto, MultipartFile imageFile) {

        Category c = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.save(imageFile);
            dto.setImg(path);
        }

        Product product = ProductMapper.toEntity(dto, c);

        if (dto.getVariants() != null) {

            dto.getVariants().forEach(v -> {

                ProductVariant variant = ProductVariantMapper.toEntity(v);

                variant.setProduct(product);

                // Calcular precio de venta
                variant.setSalePrice(
                        priceCalculatorService.calculateSalePrice(variant));

                product.getVariants().add(variant);
            });
        }

        Product saved = productRepository.save(product);

        return ProductMapper.toDTO(saved);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto, MultipartFile imageFile) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Imagen
        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.save(imageFile);
            existing.setImg(path);
        }

        // Datos generales
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setState(dto.isState());
        existing.setCategory(category);

        // Limpiar variantes actuales
        existing.getVariants().clear();

        // Agregar variantes recibidas
        if (dto.getVariants() != null) {

            dto.getVariants().forEach(v -> {

                ProductVariant variant;

                if (v.getId() != null) {

                    variant = productVariantRepository
                            .findById(v.getId())
                            .orElse(new ProductVariant());

                } else {

                    variant = new ProductVariant();
                }

                variant.setMeasure(v.getMeasure());
                variant.setPurchasePrice(v.getPurchasePrice());
                variant.setProfitMargin(v.getProfitMargin());
                variant.setSalePrice(v.getSalePrice());
                variant.setStock(v.getStock());

                variant.setProduct(existing);

                existing.getVariants().add(variant);
            });
        }

        Product saved = productRepository.save(existing);

        return ProductMapper.toDTO(saved);
    }

    public List<ProductDTO> findAll() {

        long t1 = System.currentTimeMillis();

        List<Product> products = productRepository.findAll();

        long t2 = System.currentTimeMillis();

        List<ProductDTO> dtos = products.stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());

        long t3 = System.currentTimeMillis();

        System.out.println("Consulta BD: " + (t2 - t1) + " ms");
        System.out.println("Mapeo DTO: " + (t3 - t2) + " ms");
        System.out.println("TOTAL: " + (t3 - t1) + " ms");

        return dtos;
    }

    public Page<ProductDTO> findByFilters(String search, Long categoryId, Pageable pageable) {
        // Aseguramos que el parámetro de búsqueda nunca sea nulo para el query method
        String searchParam = (search != null) ? search : "";

        Page<Product> productPage;

        // Evaluamos si el frontend envió un ID de categoría válido
        if (categoryId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryId(searchParam, categoryId,
                    pageable);
        } else {
            productPage = productRepository.findByNameContainingIgnoreCase(searchParam, pageable);
        }

        // Mapeamos los resultados usando tu mapeador estático habitual
        return productPage.map(ProductMapper::toDTO);
    }

    public ProductDTO findById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return ProductMapper.toDTO(p);
    }

    public void delete(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        productRepository.delete(existing);
    }

    @Transactional
    public void processPdf(MultipartFile file) throws IOException {

        // Leer PDF
        String texto;

        try (PDDocument document = PDDocument.load(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            texto = stripper.getText(document);
        }

        // Categoría por defecto
        Category categoriaDefault = categoryRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Categoría por defecto no encontrada"));

        // Parsear productos
        List<ProductDTO> productos = parsearProductos(texto);

        for (ProductDTO dto : productos) {

            // Buscar o crear producto
            Product product = productRepository.findByName(dto.getName())
                    .orElseGet(() -> {

                        Product nuevo = ProductMapper.toEntity(dto, categoriaDefault);

                        nuevo.setState(true);
                        nuevo.setCategory(categoriaDefault);

                        return nuevo;
                    });

            product.setName(dto.getName().trim().toUpperCase());
            product.setState(true);
            product.setCategory(categoriaDefault);

            // Recorrer todas las variantes del PDF
            for (ProductVariantDTO variantDTO : dto.getVariants()) {

                ProductVariant variant = product.getVariants()
                        .stream()
                        .filter(v -> v.getMeasure().equalsIgnoreCase(variantDTO.getMeasure()))
                        .findFirst()
                        .orElse(null);

                if (variant == null) {

                    // Crear nueva variante
                    variant = ProductVariantMapper.toEntity(variantDTO);

                    variant.setProduct(product);

                    product.getVariants().add(variant);

                } else {

                    // Actualizar variante existente
                    variant.setPurchasePrice(variantDTO.getPurchasePrice());

                    variant.setProfitMargin(variantDTO.getProfitMargin());

                    variant.setSalePrice(variantDTO.getSalePrice());

                    variant.setStock(
                            variant.getStock() + variantDTO.getStock());
                }
            }

            productRepository.save(product);
        }
    }

    private List<ProductDTO> parsearProductos(String texto) {

        List<ProductDTO> productos = new ArrayList<>();
        String[] lineas = texto.split("\\r?\\n");

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            // Detectar producto
            if (linea.matches("^\\d+[A-Z0-9]+.*")) {

                ProductDTO dto = new ProductDTO();

                String[] datos = linea.split("\\s+", 2);

                dto.setName(datos.length > 1
                        ? datos[1].trim().toUpperCase()
                        : datos[0].trim().toUpperCase());

                dto.setState(true);

                ProductVariantDTO variant = new ProductVariantDTO();
                variant.setMeasure("Único");

                if (i + 1 < lineas.length) {

                    String siguiente = lineas[i + 1].trim();
                    String[] valores = siguiente.split("\\s+");

                    try {

                        // Stock
                        for (String valor : valores) {

                            if (valor.matches("\\d+(\\.\\d+)?")) {

                                variant.setStock((int) Double.parseDouble(valor));
                                break;
                            }
                        }

                        // Precio de compra
                        for (int j = valores.length - 1; j >= 0; j--) {

                            if (valores[j].matches("\\d+(\\.\\d+)?")) {

                                BigDecimal purchasePrice = new BigDecimal(valores[j]);

                                variant.setPurchasePrice(purchasePrice);

                                break;
                            }
                        }

                    } catch (Exception e) {

                        variant.setStock(0);
                        variant.setPurchasePrice(BigDecimal.ZERO);
                    }
                }

                // Calcular margen automáticamente
                BigDecimal margin = calculateProfitMargin(
                        dto.getCategoryId(),
                        variant.getPurchasePrice());

                variant.setProfitMargin(margin);

                // Calcular precio de venta
                BigDecimal profit = variant.getPurchasePrice()
                        .multiply(margin)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                BigDecimal salePrice = variant.getPurchasePrice().add(profit);

                salePrice = salePrice
                        .divide(BigDecimal.valueOf(50), 0, RoundingMode.CEILING)
                        .multiply(BigDecimal.valueOf(50));

                variant.setSalePrice(salePrice);
                variant.setProfit(profit);

                dto.setVariants(List.of(variant));

                productos.add(dto);
            }
        }

        return productos;
    }

    public BigDecimal calProfit(ProductVariant variant) {

        if (variant.getPurchasePrice() == null ||
                variant.getProfitMargin() == null) {

            return BigDecimal.ZERO;
        }

        return variant.getPurchasePrice()
                .multiply(variant.getProfitMargin())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calFinalPrice(ProductVariant variant) {

        return variant.getPurchasePrice()
                .add(calProfit(variant));
    }

    public BigDecimal calculateProfitMargin(Integer categoryId, BigDecimal price) {

        if (categoryId != null &&
        // acquasystem awaduct
                (categoryId == 1 || categoryId == 2 || categoryId == 7 || categoryId == 14 || categoryId == 15
                        || categoryId == 19)) {
            return BigDecimal.valueOf(35);
        }
        if (categoryId != null && categoryId == 13) {
            return BigDecimal.valueOf(80);
        }

        if (categoryId != null && categoryId == 17) {
            return BigDecimal.valueOf(60);
        }

        if (price.compareTo(BigDecimal.valueOf(100)) < 0) {
            return BigDecimal.valueOf(200);
        } else if (price.compareTo(BigDecimal.valueOf(500)) < 0) {
            return BigDecimal.valueOf(120);
        } else if (price.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return BigDecimal.valueOf(100);
        } else if (price.compareTo(BigDecimal.valueOf(10000)) < 0) {
            return BigDecimal.valueOf(60);
        } else if (price.compareTo(BigDecimal.valueOf(20000)) < 0) {
            return BigDecimal.valueOf(40);
        }

        return BigDecimal.valueOf(35);
    }

}
