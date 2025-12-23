package com.billing.controller;

import com.billing.dto.ProductDto;
import com.billing.entity.UnitType;
import com.billing.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update an existing product")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @Operation(summary = "Delete a product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get a product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        ProductDto product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }
    
    @Operation(summary = "Get all products with pagination")
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        Page<ProductDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @Operation(summary = "Search products by keyword")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(@RequestParam String keyword, Pageable pageable) {
        Page<ProductDto> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }
    
    @Operation(summary = "Get products by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        List<ProductDto> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    @Operation(summary = "Get all product categories")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getCategories();
        return ResponseEntity.ok(categories);
    }
    
    @Operation(summary = "Get all unit types")
    @GetMapping("/units")
    public ResponseEntity<List<UnitType>> getUnitTypes() {
        List<UnitType> unitTypes = Arrays.asList(UnitType.values());
        return ResponseEntity.ok(unitTypes);
    }
    
    @Operation(summary = "Get low stock products")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDto>> getLowStockProducts() {
        List<ProductDto> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @Operation(summary = "Bulk upload products via CSV")
    @PostMapping("/bulk-upload")
    public ResponseEntity<List<ProductDto>> bulkUploadProducts(@RequestParam("file") MultipartFile file) throws IOException {
        List<ProductDto> uploadedProducts = productService.bulkUploadProducts(file);
        return ResponseEntity.ok(uploadedProducts);
    }
}
