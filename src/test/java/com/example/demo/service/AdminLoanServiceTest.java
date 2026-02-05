package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.dto.AdminLoanApplicationDTO;
import com.example.demo.entity.Branch;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.UserProfileRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AdminLoanServiceTest {

  @Mock private LoanApplicationRepository loanApplicationRepository;
  @Mock private UserProfileRepository userProfileRepository;

  @InjectMocks private AdminLoanService adminLoanService;

  private User testUser;
  private UserProfile testUserProfile;
  private Product testProduct;
  private Branch testBranch;
  private LoanApplication testLoanApplication;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("Test User");
    testUser.setEmail("test@example.com");

    testUserProfile = new UserProfile();
    testUserProfile.setUserId(1L);
    testUserProfile.setNik("1234567890123456");
    testUserProfile.setPhoneNumber("081234567890");
    testUserProfile.setBankName("BCA");
    testUserProfile.setAccountNumber("1234567890");

    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setName("Bronze Tier");

    testBranch = new Branch();
    testBranch.setId(1L);
    testBranch.setName("Jakarta Branch");

    testLoanApplication = new LoanApplication();
    testLoanApplication.setLoanApplicationId(1L);
    testLoanApplication.setUser(testUser);
    testLoanApplication.setProduct(testProduct);
    testLoanApplication.setBranch(testBranch);
    testLoanApplication.setAmount(5000000.0);
    testLoanApplication.setTenureMonths(12);
    testLoanApplication.setInterestRateApplied(12.0);
    testLoanApplication.setTotalAmountToPay(5661360.0);
    testLoanApplication.setCurrentStatus(LoanStatus.SUBMITTED.name());
    testLoanApplication.setCreatedAt(LocalDateTime.now());
    testLoanApplication.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void getAllLoanApplications_WithPagination_ShouldReturnPageOfLoans() {
    // Arrange
    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<LoanApplication> loanPage = new PageImpl<>(List.of(testLoanApplication));

    when(loanApplicationRepository.findAll(any(PageRequest.class))).thenReturn(loanPage);
    when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testUserProfile));

    // Act
    Page<AdminLoanApplicationDTO> result = adminLoanService.getAllLoanApplications(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    AdminLoanApplicationDTO dto = result.getContent().get(0);
    assertEquals(1L, dto.getLoanApplicationId());
    assertEquals("Test User", dto.getUserName());
    assertEquals("test@example.com", dto.getUserEmail());
    assertEquals("1234567890123456", dto.getNik());
    assertEquals("MARKETING", dto.getCurrentBucket());
  }

  @Test
  void getAllLoanApplications_WithoutProfile_ShouldReturnNullProfileFields() {
    // Arrange
    PageRequest pageable = PageRequest.of(0, 10);
    Page<LoanApplication> loanPage = new PageImpl<>(List.of(testLoanApplication));

    when(loanApplicationRepository.findAll(any(PageRequest.class))).thenReturn(loanPage);
    when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());

    // Act
    Page<AdminLoanApplicationDTO> result = adminLoanService.getAllLoanApplications(pageable);

    // Assert
    AdminLoanApplicationDTO dto = result.getContent().get(0);
    assertNull(dto.getNik());
    assertNull(dto.getPhoneNumber());
    assertNull(dto.getBankName());
  }

  @Test
  void determineBucket_ShouldReturnMarketing_ForSubmittedStatus() {
    assertEquals("MARKETING", adminLoanService.determineBucket(LoanStatus.SUBMITTED.name()));
  }

  @Test
  void determineBucket_ShouldReturnMarketing_ForInReviewStatus() {
    assertEquals("MARKETING", adminLoanService.determineBucket(LoanStatus.IN_REVIEW.name()));
  }

  @Test
  void determineBucket_ShouldReturnBranchManager_ForWaitingApprovalStatus() {
    assertEquals(
        "BRANCH_MANAGER", adminLoanService.determineBucket(LoanStatus.WAITING_APPROVAL.name()));
  }

  @Test
  void determineBucket_ShouldReturnBackOffice_ForApprovedWaitingDisbursementStatus() {
    assertEquals(
        "BACK_OFFICE",
        adminLoanService.determineBucket(LoanStatus.APPROVED_WAITING_DISBURSEMENT.name()));
  }

  @Test
  void determineBucket_ShouldReturnCompleted_ForDisbursedStatus() {
    assertEquals("COMPLETED", adminLoanService.determineBucket(LoanStatus.DISBURSED.name()));
  }

  @Test
  void determineBucket_ShouldReturnCompleted_ForPaidStatus() {
    assertEquals("COMPLETED", adminLoanService.determineBucket(LoanStatus.PAID.name()));
  }

  @Test
  void determineBucket_ShouldReturnCompleted_ForRejectedStatus() {
    assertEquals("COMPLETED", adminLoanService.determineBucket(LoanStatus.REJECTED.name()));
  }

  @Test
  void determineBucket_ShouldReturnUnknown_ForUnknownStatus() {
    assertEquals("UNKNOWN", adminLoanService.determineBucket("SOME_UNKNOWN_STATUS"));
  }
}
