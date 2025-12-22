package com.example.demo.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @CacheEvict(value = { "products", "activeProducts" }, allEntries = true)
    public Product createProduct(Product product) {
        // Validate unique code
        if (productRepository.findByCode(product.getCode()).isPresent()) {
            throw new IllegalArgumentException("Product with code '" + product.getCode() + "' already exists");
        }

        // Validate amount ranges
        if (product.getMinAmount() >= product.getMaxAmount()) {
            throw new IllegalArgumentException("Minimum amount must be less than maximum amount");
        }

        // Validate tenure ranges
        if (product.getMinTenureMonths() >= product.getMaxTenureMonths()) {
            throw new IllegalArgumentException("Minimum tenure must be less than maximum tenure");
        }

        return productRepository.save(product);
    }

    @Cacheable(value = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Cacheable(value = "activeProducts")
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActive(true);
    }

    @CacheEvict(value = { "products", "activeProducts", "productByCode" }, allEntries = true)
    public Product updateProductStatus(Long id, Boolean isActive) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setIsActive(isActive);
        return productRepository.save(product);
    }

    @Cacheable(value = "productByCode", key = "#code")
    public Product getProductByCode(String code) {
        return productRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with code: " + code));
    }
}
