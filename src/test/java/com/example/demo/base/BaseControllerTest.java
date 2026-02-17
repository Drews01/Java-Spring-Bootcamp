package com.example.demo.base;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.constants.ApiMessage;
import com.example.demo.constants.ErrorMessage;
import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.security.CustomUserDetails;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseControllerTest {

  private TestBaseController controller;
  private SecurityContext securityContext;
  private Authentication authentication;

  // Concrete implementation for testing the abstract class
  private static class TestBaseController extends BaseController {
    // No additional methods needed for testing
  }

  @BeforeEach
  void setUp() {
    controller = new TestBaseController();
    securityContext = mock(SecurityContext.class);
    authentication = mock(Authentication.class);
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void testGetCurrentUserIdWithValidAuthentication() {
    // Arrange
    Role role = Role.builder().id(1L).name("USER").build();
    User user =
        User.builder()
            .id(123L)
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .isActive(true)
            .roles(new HashSet<>(Arrays.asList(role)))
            .build();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);

    // Act
    Long userId = controller.getCurrentUserId();

    // Assert
    assertNotNull(userId);
    assertEquals(123L, userId);
    verify(securityContext).getAuthentication();
    verify(authentication, times(2)).getPrincipal();
  }

  @Test
  void testGetCurrentUserIdThrowsExceptionWhenNoAuthentication() {
    // Arrange
    when(securityContext.getAuthentication()).thenReturn(null);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> controller.getCurrentUserId());

    assertEquals(ErrorMessage.NO_USER_IN_CONTEXT, exception.getMessage());
    verify(securityContext).getAuthentication();
  }

  @Test
  void testGetCurrentUserIdThrowsExceptionWhenPrincipalIsNotCustomUserDetails() {
    // Arrange
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn("not-a-custom-user-details");

    // Act & Assert
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> controller.getCurrentUserId());

    assertEquals(ErrorMessage.NO_USER_IN_CONTEXT, exception.getMessage());
    verify(securityContext).getAuthentication();
    verify(authentication).getPrincipal();
  }

  @Test
  void testBuildPageResponseConstructsCorrectStructure() {
    // Arrange
    List<String> content = Arrays.asList("item1", "item2", "item3");
    Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 23);

    // Act
    ApiResponse<PagedResponse<String>> response = controller.buildPageResponse(page);

    // Assert
    assertNotNull(response);
    assertTrue(response.getSuccess());
    assertEquals(ApiMessage.DATA_RETRIEVED, response.getMessage());
    assertEquals(200, response.getStatusCode());

    PagedResponse<String> pagedResponse = response.getData();
    assertNotNull(pagedResponse);
    assertEquals(content, pagedResponse.getContent());
    assertEquals(0, pagedResponse.getPageNumber());
    assertEquals(10, pagedResponse.getPageSize());
    assertEquals(23, pagedResponse.getTotalElements());
    assertEquals(3, pagedResponse.getTotalPages());
    assertFalse(pagedResponse.isLast());
  }

  @Test
  void testBuildPageResponseWithLastPage() {
    // Arrange
    List<String> content = Arrays.asList("item1", "item2");
    Page<String> page = new PageImpl<>(content, PageRequest.of(2, 10), 22);

    // Act
    ApiResponse<PagedResponse<String>> response = controller.buildPageResponse(page);

    // Assert
    assertNotNull(response);
    PagedResponse<String> pagedResponse = response.getData();
    assertTrue(pagedResponse.isLast());
    assertEquals(2, pagedResponse.getPageNumber());
    assertEquals(2, pagedResponse.getContent().size());
  }

  @Test
  void testBuildPageResponseWithEmptyPage() {
    // Arrange
    List<String> content = Arrays.asList();
    Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 0);

    // Act
    ApiResponse<PagedResponse<String>> response = controller.buildPageResponse(page);

    // Assert
    assertNotNull(response);
    PagedResponse<String> pagedResponse = response.getData();
    assertNotNull(pagedResponse.getContent());
    assertTrue(pagedResponse.getContent().isEmpty());
    assertEquals(0, pagedResponse.getTotalElements());
    assertEquals(0, pagedResponse.getTotalPages());
    assertTrue(pagedResponse.isLast());
  }

  @Test
  void testBuildPageResponseWithDifferentDataTypes() {
    // Arrange
    List<Integer> content = Arrays.asList(1, 2, 3, 4, 5);
    Page<Integer> page = new PageImpl<>(content, PageRequest.of(1, 5), 15);

    // Act
    ApiResponse<PagedResponse<Integer>> response = controller.buildPageResponse(page);

    // Assert
    assertNotNull(response);
    PagedResponse<Integer> pagedResponse = response.getData();
    assertEquals(5, pagedResponse.getContent().size());
    assertEquals(1, pagedResponse.getPageNumber());
    assertEquals(5, pagedResponse.getPageSize());
    assertEquals(15, pagedResponse.getTotalElements());
    assertEquals(3, pagedResponse.getTotalPages());
  }
}
