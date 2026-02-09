package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Transactional
  @CacheEvict(
      value = {"products", "activeProducts"},
      allEntries = true)
  public ProductDTO createProduct(Product product) {
    // Validate unique code (check universally to prevent DB constraint violation)
    if (productRepository.findByCode(product.getCode()).isPresent()) {
      throw new IllegalArgumentException(
          "Product with code '" + product.getCode() + "' already exists (it might be deleted)");
    }

    // Validate amount ranges
    if (product.getMinAmount() >= product.getMaxAmount()) {
      throw new IllegalArgumentException("Minimum amount must be less than maximum amount");
    }

    // Validate tenure ranges
    if (product.getMinTenureMonths() >= product.getMaxTenureMonths()) {
      throw new IllegalArgumentException("Minimum tenure must be less than maximum tenure");
    }

    Product saved = productRepository.save(product);
    return ProductDTO.fromEntity(saved);
  }

  @Cacheable(value = "products")
  public List<ProductDTO> getAllProducts() {
    return productRepository.findByDeletedFalse().stream()
        .map(ProductDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Cacheable(value = "activeProducts")
  public List<ProductDTO> getActiveProducts() {
    return productRepository.findByIsActiveTrueAndDeletedFalse().stream()
        .map(ProductDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  @CacheEvict(
      value = {"products", "activeProducts", "productByCode"},
      allEntries = true)
  public ProductDTO updateProductStatus(Long id, Boolean isActive) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    product.setIsActive(isActive);
    Product saved = productRepository.save(product);
    return ProductDTO.fromEntity(saved);
  }

  @Cacheable(value = "productByCode", key = "#code")
  public ProductDTO getProductByCode(String code) {
    Product product =
        productRepository
            .findByCodeAndDeletedFalse(code)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "code", code));
    return ProductDTO.fromEntity(product);
  }

  @Transactional
  @CacheEvict(
      value = {"products", "activeProducts", "productByCode"},
      allEntries = true)
  public void deleteProduct(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    product.setDeleted(true);
    product.setIsActive(false); // Optionally set active to false
    productRepository.save(product);
  }
}
