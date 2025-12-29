package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseUtil.created(createdProduct, "Product created successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseUtil.ok(products, "Products retrieved successfully");
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Product>>> getActiveProducts() {
        List<Product> products = productService.getActiveProducts();
        return ResponseUtil.ok(products, "Active products retrieved successfully");
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Product>> getProductByCode(@PathVariable String code) {
        Product product = productService.getProductByCode(code);
        return ResponseUtil.ok(product, "Product retrieved successfully");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Product>> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        Product updatedProduct = productService.updateProductStatus(id, isActive);
        return ResponseUtil.ok(updatedProduct, "Product status updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseUtil.okMessage("Product deleted successfully");
    }
}
