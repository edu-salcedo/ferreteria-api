package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.dto.productDto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.mapper.ProductMapper;
import com.ferreteria_edu.ferreteria_api.model.Category;
import com.ferreteria_edu.ferreteria_api.model.Product;
import com.ferreteria_edu.ferreteria_api.repository.CategoryRepository;
import com.ferreteria_edu.ferreteria_api.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public ProductDTO create(ProductDTO dto, MultipartFile imageFile) {

        Category c = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.save(imageFile);
            dto.setImg(path);
        }

        Product p = ProductMapper.toEntity(dto, c);
        return ProductMapper.toDTO(productRepository.save(p));
    }

    public ProductDTO update(Long id, ProductDTO dto, MultipartFile imageFile) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Category c = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Si hay nueva imagen, la guardamos
        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.save(imageFile);
            existing.setImg(path);
        } else {
            existing.setImg(dto.getImg());
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        existing.setState(dto.isState());
        existing.setCategory(c);

        return ProductMapper.toDTO(productRepository.save(existing));
    }

    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
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

        // 1️⃣ Leer PDF
        String texto;
        try (PDDocument document = PDDocument.load(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            texto = stripper.getText(document);
        }

        // 2️⃣ Parsear PDF a DTOs
        List<ProductDTO> productos = parsearProductos(texto);

        // 3️⃣ Guardar productos en la base de datos
        Category categoriaDefault = categoryRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Categoría por defecto no encontrada"));

        for (ProductDTO dto : productos) {
            // Buscar producto existente por name
            Product product = productRepository.findByName(dto.getName())
                    .orElseGet(() -> ProductMapper.toEntity(dto, categoriaDefault));

            // Actualizar campos importantes
            product.setName(dto.getName().trim().toUpperCase());
            product.setPrice(dto.getPrice());
            product.setStock(product.getStock() + dto.getStock());
            product.setState(true);
            product.setCategory(categoriaDefault);

            productRepository.save(product);
        }
    }

        // 4️⃣ Parser básico según tu formato

    private List<ProductDTO> parsearProductos(String texto) {
        List<ProductDTO> productos = new ArrayList<>();
        String[] lineas = texto.split("\\r?\\n");

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i].trim();

            // Detectar línea de producto por el ID (empieza con dígito)
            if (linea.matches("^\\d+[A-Z0-9]+.*")) {
                ProductDTO dto = new ProductDTO();

                // La descripción completa está después del ID
                String[] datos = linea.split("\\s+", 2);
                dto.setName(datos.length > 1 ? datos[1].trim() : datos[0].trim());

                // La siguiente línea contiene stock y precio
                if (i + 1 < lineas.length) {
                    String siguiente = lineas[i + 1].trim();
                    String[] valores = siguiente.split("\\s+");

                    try {
                        // Stock: la primera columna numérica de la línea
                        for (String val : valores) {
                            if (val.matches("\\d+(\\.\\d+)?")) {
                                dto.setStock((int) Double.parseDouble(val));
                                break;
                            }
                        }

                        // Precio: tomar el último valor numérico de la línea
                        for (int j = valores.length - 1; j >= 0; j--) {
                            if (valores[j].matches("\\d+(\\.\\d+)?")) {
                                dto.setPrice(new BigDecimal(valores[j]));
                                break;
                            }
                        }

                        dto.setState(true);
                    } catch (Exception e) {
                        dto.setStock(0);
                        dto.setPrice(BigDecimal.ZERO);
                    }
                }

                productos.add(dto);
            }
        }

        return productos;
    }

    public BigDecimal calProfit(Product p) {
        if (p.getProfitMargin() == null) return BigDecimal.ZERO;

        return p.getPrice()
                .multiply(p.getProfitMargin())
                .divide(BigDecimal.valueOf(100));
    }

    public BigDecimal calFinalPrice(Product p) {
        return p.getPrice().add(calProfit(p));
    }

   public BigDecimal calculateProfitMargin(Integer categoryId, BigDecimal price) {

            if (categoryId != null &&
                    (categoryId == 2 || categoryId == 3  || categoryId == 8 || categoryId == 15 || categoryId == 16 || categoryId == 19)) {
                return BigDecimal.valueOf(35);
            }

            if (price.compareTo(BigDecimal.valueOf(100)) < 0) {
                return BigDecimal.valueOf(200);
            } else if (price.compareTo(BigDecimal.valueOf(500)) < 0) {
                return BigDecimal.valueOf(100);
            } else if (price.compareTo(BigDecimal.valueOf(1000)) < 0) {
                return BigDecimal.valueOf(70);
            } else if (price.compareTo(BigDecimal.valueOf(10000)) < 0) {
                return BigDecimal.valueOf(40);
            } else if (price.compareTo(BigDecimal.valueOf(20000)) < 0) {
                return BigDecimal.valueOf(30);
            }

            return BigDecimal.valueOf(25);
        }


}
