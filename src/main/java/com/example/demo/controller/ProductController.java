package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import com.example.demo.base.ApiResponse;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        ApiResponse<Product> response = ApiResponse.created(
                createdProduct,
                "Product created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        ApiResponse<List<Product>> response = ApiResponse.success(
                products,
                "Products retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Product>>> getActiveProducts() {
        List<Product> products = productService.getActiveProducts();
        ApiResponse<List<Product>> response = ApiResponse.success(
                products,
                "Active products retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Product>> getProductByCode(@PathVariable String code) {
        Product product = productService.getProductByCode(code);
        ApiResponse<Product> response = ApiResponse.success(
                product,
                "Product retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Product>> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        Product updatedProduct = productService.updateProductStatus(id, isActive);
        ApiResponse<Product> response = ApiResponse.success(
                updatedProduct,
                "Product status updated successfully");
        return ResponseEntity.ok(response);
    }
}
