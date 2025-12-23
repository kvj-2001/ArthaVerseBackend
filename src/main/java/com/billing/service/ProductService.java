package com.billing.service;

import com.billing.dto.ProductDto;
import com.billing.entity.Product;
import com.billing.entity.UnitType;
import com.billing.entity.User;
import com.billing.exception.BadRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.repository.ProductRepository;
import com.billing.repository.UserRepository;
import com.billing.security.UserPrincipal;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    private String generateUniqueProductCode(Long userId) {
        String prefix = "PRD";
        String code;
        int counter = 1;
        
        do {
            code = prefix + String.format("%06d", counter);
            counter++;
        } while (productRepository.findByCodeAndUserId(code, userId).isPresent());
        
        return code;
    }
    
    public ProductDto createProduct(ProductDto productDto) {
        Long userId = getCurrentUserId();
        
        Product product = modelMapper.map(productDto, Product.class);
        
        // Auto-generate unique product code
        String generatedCode = generateUniqueProductCode(userId);
        product.setCode(generatedCode);
        product.setUserId(userId);
        
        // Convert string unit to enum
        if (productDto.getUnit() != null) {
            product.setUnit(UnitType.fromString(productDto.getUnit()));
        }
        
        Product savedProduct = productRepository.save(product);
        
        logger.info("Product created: {} by user: {}", savedProduct.getCode(), userId);
        
        return convertToDto(savedProduct);
    }
    
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Long userId = getCurrentUserId();
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this product");
        }
        
        // Map only the updatable fields, preserving the existing code
        String existingCode = product.getCode();
        modelMapper.map(productDto, product);
        product.setCode(existingCode); // Keep the original auto-generated code
        product.setUserId(userId); // Ensure userId doesn't get overwritten
        
        // Convert string unit to enum
        if (productDto.getUnit() != null) {
            product.setUnit(UnitType.fromString(productDto.getUnit()));
        }
        
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Product updated: {} by user: {}", updatedProduct.getCode(), userId);
        
        return convertToDto(updatedProduct);
    }
    
    public void deleteProduct(Long id) {
        Long userId = getCurrentUserId();
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this product");
        }
        
        productRepository.delete(product);
        
        logger.info("Product deleted: {} by user: {}", product.getCode(), userId);
    }
    
    public ProductDto getProduct(Long id) {
        Long userId = getCurrentUserId();
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to access this product");
        }
        
        return convertToDto(product);
    }
    
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        Long userId = getCurrentUserId();
        
        Page<Product> products = productRepository.findByUserIdAndActive(userId, true, pageable);
        
        return products.map(this::convertToDto);
    }
    
    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {
        Long userId = getCurrentUserId();
        
        Page<Product> products = productRepository.searchProducts(userId, keyword, pageable);
        
        return products.map(this::convertToDto);
    }
    
    public List<ProductDto> getProductsByCategory(String category) {
        Long userId = getCurrentUserId();
        
        List<Product> products = productRepository.findByUserIdAndCategory(userId, category);
        
        return products.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<String> getCategories() {
        Long userId = getCurrentUserId();
        
        return productRepository.findCategoriesByUserId(userId);
    }
    
    public List<ProductDto> getLowStockProducts() {
        Long userId = getCurrentUserId();
        
        List<Product> products = productRepository.findLowStockProducts(userId);
        
        return products.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<ProductDto> bulkUploadProducts(MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();
        
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        
        List<ProductDto> uploadedProducts = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVParser csvParser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .parse(reader);
            
            for (CSVRecord csvRecord : csvParser) {
                try {
                    ProductDto productDto = new ProductDto();
                    // Don't set code from CSV - it will be auto-generated
                    productDto.setName(csvRecord.get("name"));
                    productDto.setDescription(csvRecord.get("description"));
                    productDto.setPrice(new BigDecimal(csvRecord.get("price")));
                    productDto.setMrp(new BigDecimal(csvRecord.get("mrp")));
                    productDto.setQuantity(Integer.parseInt(csvRecord.get("quantity")));
                    productDto.setMinStockLevel(Integer.parseInt(csvRecord.get("minStockLevel")));
                    productDto.setCategory(csvRecord.get("category"));
                    productDto.setUnit(csvRecord.get("unit"));
                    productDto.setActive(true); // Explicitly set active to true
                    
                    Product product = modelMapper.map(productDto, Product.class);
                    
                    // Auto-generate unique product code
                    String generatedCode = generateUniqueProductCode(userId);
                    product.setCode(generatedCode);
                    product.setUserId(userId);
                    product.setActive(true); // Explicitly set active to true on entity as well
                    
                    // Convert string unit to enum
                    if (productDto.getUnit() != null) {
                        product.setUnit(UnitType.fromString(productDto.getUnit()));
                    }
                    
                    Product savedProduct = productRepository.save(product);
                    uploadedProducts.add(convertToDto(savedProduct));
                    
                    logger.debug("Successfully processed product: {} with code: {}", product.getName(), product.getCode());
                    
                } catch (Exception e) {
                    logger.error("Error processing CSV record: {}", csvRecord, e);
                }
            }
        }
        
        logger.info("Bulk upload completed. {} products uploaded by user: {}", uploadedProducts.size(), userId);
        
        return uploadedProducts;
    }
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        if (product.getUnit() != null) {
            dto.setUnit(product.getUnit().name());
        }
        return dto;
    }
}
