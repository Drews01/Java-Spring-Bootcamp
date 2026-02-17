package com.example.demo.base;

import com.example.demo.constants.ApiMessage;
import com.example.demo.constants.ErrorMessage;
import com.example.demo.dto.PagedResponse;
import com.example.demo.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract base controller providing common functionality for all controllers. Eliminates code
 * duplication and ensures consistent patterns across the application.
 */
public abstract class BaseController {

  /**
   * Extract authenticated user ID from security context. Prevents IDOR vulnerabilities by ensuring
   * user ID comes from JWT token.
   *
   * @return authenticated user's ID
   * @throws IllegalStateException if no authenticated user in context
   */
  protected Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getId();
    }
    throw new IllegalStateException(ErrorMessage.NO_USER_IN_CONTEXT);
  }

  /**
   * Build paginated API response with metadata.
   *
   * @param page Spring Data Page object
   * @param <T> type of page content
   * @return ApiResponse containing page data and metadata
   */
  protected <T> ApiResponse<PagedResponse<T>> buildPageResponse(Page<T> page) {
    PagedResponse<T> pagedResponse =
        PagedResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();

    return ApiResponse.success(pagedResponse, ApiMessage.DATA_RETRIEVED);
  }
}
