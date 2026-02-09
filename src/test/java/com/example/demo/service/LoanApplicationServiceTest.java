package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.LoanStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
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
public class LoanApplicationServiceTest {

  @Mock private LoanApplicationRepository loanApplicationRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;

  @InjectMocks private LoanApplicationService loanApplicationService;

  private User user;
  private Product product;
  private LoanApplication loanApplication;

  @BeforeEach
  void setUp() {
    user = User.builder().id(1L).username("testuser").build();
    product = Product.builder().id(1L).name("Bronze").build();
    loanApplication =
        LoanApplication.builder()
            .loanApplicationId(100L)
            .user(user)
            .product(product)
            .amount(1000000.0)
            .currentStatus(LoanStatus.SUBMITTED.name())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  @Test
  void createLoanApplication_ShouldSaveAndReturnDTO() {
    // Arrange
    LoanApplicationDTO dto =
        LoanApplicationDTO.builder()
            .userId(user.getId())
            .productId(product.getId())
            .amount(1000000.0)
            .build();

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

    // Act
    LoanApplicationDTO result = loanApplicationService.createLoanApplication(dto);

    // Assert
    assertNotNull(result);
    assertEquals(1000000.0, result.getAmount());
    assertEquals("Bronze", result.getProductName());
  }

  @Test
  void getLoanApplication_WhenExists_ShouldReturnDTO() {
    // Arrange
    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));

    // Act
    LoanApplicationDTO result = loanApplicationService.getLoanApplication(100L);

    // Assert
    assertNotNull(result);
    assertEquals(100L, result.getLoanApplicationId());
  }

  @Test
  void getLoanApplication_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(loanApplicationRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> loanApplicationService.getLoanApplication(999L));
  }

  @Test
  void getLoanApplicationsByUserId_ShouldReturnUserLoans() {
    // Arrange
    when(loanApplicationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()))
        .thenReturn(Collections.singletonList(loanApplication));

    // Act
    List<LoanApplicationDTO> result =
        loanApplicationService.getLoanApplicationsByUserId(user.getId());

    // Assert
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(user.getId(), result.get(0).getUserId());
  }

  @Test
  void getAllLoanApplications_ShouldReturnAllLoans() {
    // Arrange
    when(loanApplicationRepository.findAll())
        .thenReturn(Collections.singletonList(loanApplication));

    // Act
    List<LoanApplicationDTO> result = loanApplicationService.getAllLoanApplications();

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void updateLoanApplication_ShouldUpdateFields() {
    // Arrange
    LoanApplicationDTO dto =
        LoanApplicationDTO.builder()
            .amount(2000000.0)
            .tenureMonths(24)
            .currentStatus(LoanStatus.SUBMITTED.name())
            .build();

    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));
    when(loanApplicationRepository.save(any(LoanApplication.class)))
        .thenAnswer(i -> i.getArguments()[0]);

    // Act
    LoanApplicationDTO result = loanApplicationService.updateLoanApplication(100L, dto);

    // Assert
    assertEquals(2000000.0, result.getAmount());
    assertEquals(24, result.getTenureMonths());
    assertEquals(LoanStatus.SUBMITTED.name(), result.getCurrentStatus());
  }
}
