package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@RequestBody Product product) {
    ProductDTO createdProduct = productService.createProduct(product);
    return ResponseUtil.created(createdProduct, "Product created successfully");
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
    List<ProductDTO> products = productService.getAllProducts();
    return ResponseUtil.ok(products, "Products retrieved successfully");
  }

  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<ProductDTO>>> getActiveProducts() {
    List<ProductDTO> products = productService.getActiveProducts();
    return ResponseUtil.ok(products, "Active products retrieved successfully");
  }

  @GetMapping("/code/{code}")
  public ResponseEntity<ApiResponse<ProductDTO>> getProductByCode(@PathVariable String code) {
    ProductDTO product = productService.getProductByCode(code);
    return ResponseUtil.ok(product, "Product retrieved successfully");
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse<ProductDTO>> updateProductStatus(
      @PathVariable Long id, @RequestParam Boolean isActive) {
    ProductDTO updatedProduct = productService.updateProductStatus(id, isActive);
    return ResponseUtil.ok(updatedProduct, "Product status updated successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseUtil.okMessage("Product deleted successfully");
  }
}
