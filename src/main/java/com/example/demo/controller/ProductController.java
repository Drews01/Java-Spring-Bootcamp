package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
