package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class ProductServiceTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductService productService;

  private Product product;

  @BeforeEach
  void setUp() {
    product =
        Product.builder()
            .id(1L)
            .name("Test Product")
            .code("TP1")
            .minAmount(1000.0)
            .maxAmount(10000.0)
            .minTenureMonths(3)
            .maxTenureMonths(12)
            .isActive(true)
            .deleted(false)
            .build();
  }

  @Test
  void createProduct_WithValidData_ShouldSave() {
    // Arrange
    when(productRepository.findByCode("TP1")).thenReturn(Optional.empty());
    when(productRepository.save(any(Product.class))).thenReturn(product);

    // Act
    ProductDTO result = productService.createProduct(product);

    // Assert
    assertNotNull(result);
    assertEquals("TP1", result.getCode());
  }

  @Test
  void createProduct_WithDuplicateCode_ShouldThrowException() {
    // Arrange
    when(productRepository.findByCode("TP1")).thenReturn(Optional.of(product));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
  }

  @Test
  void createProduct_WhenMinAmountGreaterThanMax_ShouldThrowException() {
    // Arrange
    product.setMinAmount(20000.0);
    product.setMaxAmount(10000.0);
    when(productRepository.findByCode("TP1")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
  }

  @Test
  void createProduct_WhenMinTenureGreaterThanMax_ShouldThrowException() {
    // Arrange
    product.setMinTenureMonths(20);
    product.setMaxTenureMonths(10);
    when(productRepository.findByCode("TP1")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
  }

  @Test
  void getAllProducts_ShouldReturnNonDeleted() {
    // Arrange
    when(productRepository.findByDeletedFalse()).thenReturn(Collections.singletonList(product));

    // Act
    List<ProductDTO> result = productService.getAllProducts();

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void getActiveProducts_ShouldReturnOnlyActive() {
    // Arrange
    when(productRepository.findByIsActiveTrueAndDeletedFalse())
        .thenReturn(Collections.singletonList(product));

    // Act
    List<ProductDTO> result = productService.getActiveProducts();

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void updateProductStatus_ShouldUpdateAndSave() {
    // Arrange
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

    // Act
    ProductDTO result = productService.updateProductStatus(1L, false);

    // Assert
    assertFalse(result.getIsActive());
  }

  @Test
  void deleteProduct_ShouldSoftDelete() {
    // Arrange
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

    // Act
    productService.deleteProduct(1L);

    // Assert
    assertTrue(product.getDeleted());
    assertFalse(product.getIsActive());
  }
}
