package com.example.demo.base;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.constants.ApiMessage;
import com.example.demo.dto.PagedResponse;
import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Property-based tests for BaseController using jqwik. These tests validate universal properties
 * that should hold for all possible inputs.
 */
public class BaseControllerPropertyTest {

  private TestBaseController controller;

  // Concrete implementation for testing the abstract class
  private static class TestBaseController extends BaseController {
    // No additional methods needed for testing
  }

  /**
   * Property 1: Page Response Structure Completeness
   *
   * <p>**Validates: Requirements 2.5**
   *
   * <p>This property verifies that buildPageResponse always constructs a complete and correct
   * PagedResponse structure regardless of input variations. The method must:
   *
   * <ul>
   *   <li>Return a non-null ApiResponse wrapper
   *   <li>Set success flag to true
   *   <li>Include the standard success message
   *   <li>Set HTTP status code to 200
   *   <li>Contain a non-null PagedResponse data object
   *   <li>Preserve all content from the input page
   *   <li>Correctly map page metadata (number, size, total elements, total pages)
   *   <li>Accurately reflect the last page flag
   * </ul>
   */
  @Property
  void buildPageResponseAlwaysConstructsCompleteStructure(@ForAll("pageData") PageData pageData) {
    // Arrange
    controller = new TestBaseController();
    Page<String> page =
        new PageImpl<>(
            pageData.content,
            PageRequest.of(pageData.pageNumber, pageData.pageSize),
            pageData.totalElements);

    // Act
    ApiResponse<PagedResponse<String>> response = controller.buildPageResponse(page);

    // Assert - ApiResponse wrapper structure
    assertNotNull(response, "ApiResponse should never be null");
    assertTrue(response.getSuccess(), "Success flag should always be true");
    assertEquals(
        ApiMessage.DATA_RETRIEVED,
        response.getMessage(),
        "Message should be the standard success message");
    assertEquals(200, response.getStatusCode(), "Status code should always be 200");
    assertNotNull(response.getTimestamp(), "Timestamp should always be set");

    // Assert - PagedResponse data structure
    PagedResponse<String> pagedResponse = response.getData();
    assertNotNull(pagedResponse, "PagedResponse data should never be null");

    // Assert - Content preservation
    assertNotNull(pagedResponse.getContent(), "Content list should never be null");
    assertEquals(
        pageData.content.size(),
        pagedResponse.getContent().size(),
        "Content size should match input");
    assertEquals(
        pageData.content, pagedResponse.getContent(), "Content should be preserved exactly");

    // Assert - Page metadata correctness
    assertEquals(
        pageData.pageNumber,
        pagedResponse.getPageNumber(),
        "Page number should match input page number");
    assertEquals(
        pageData.pageSize, pagedResponse.getPageSize(), "Page size should match input page size");
    assertEquals(
        pageData.totalElements,
        pagedResponse.getTotalElements(),
        "Total elements should match input total");

    // Assert - Calculated fields
    int expectedTotalPages = (int) Math.ceil((double) pageData.totalElements / pageData.pageSize);
    assertEquals(
        expectedTotalPages,
        pagedResponse.getTotalPages(),
        "Total pages should be correctly calculated");

    boolean expectedIsLast = (pageData.pageNumber + 1) >= expectedTotalPages;
    assertEquals(
        expectedIsLast, pagedResponse.isLast(), "Last page flag should be correctly determined");
  }

  /**
   * Provides arbitrary page data for property testing. Generates realistic pagination scenarios
   * with constrained values.
   */
  @Provide
  Arbitrary<PageData> pageData() {
    Arbitrary<Integer> pageSize = Arbitraries.integers().between(1, 100);
    Arbitrary<Long> totalElements = Arbitraries.longs().between(0, 1000);

    return Combinators.combine(pageSize, totalElements)
        .as(
            (size, total) -> {
              // Calculate valid page number range
              int maxPage = total == 0 ? 0 : (int) Math.ceil((double) total / size) - 1;
              int pageNum = maxPage == 0 ? 0 : Arbitraries.integers().between(0, maxPage).sample();

              // Generate content for current page
              int contentSize = calculateContentSize(pageNum, size, total);
              List<String> content = generateContent(contentSize);

              return new PageData(content, pageNum, size, total);
            });
  }

  /**
   * Calculate how many items should be on a specific page given pagination parameters.
   *
   * @param pageNumber zero-based page number
   * @param pageSize items per page
   * @param totalElements total items across all pages
   * @return number of items on this specific page
   */
  private int calculateContentSize(int pageNumber, int pageSize, long totalElements) {
    if (totalElements == 0) {
      return 0;
    }

    long startIndex = (long) pageNumber * pageSize;
    if (startIndex >= totalElements) {
      return 0;
    }

    long remainingElements = totalElements - startIndex;
    return (int) Math.min(pageSize, remainingElements);
  }

  /**
   * Generate sample content for testing.
   *
   * @param size number of items to generate
   * @return list of sample strings
   */
  private List<String> generateContent(int size) {
    List<String> content = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      content.add("item-" + i);
    }
    return content;
  }

  /** Data class to hold page parameters for property testing. */
  private static class PageData {
    final List<String> content;
    final int pageNumber;
    final int pageSize;
    final long totalElements;

    PageData(List<String> content, int pageNumber, int pageSize, long totalElements) {
      this.content = content;
      this.pageNumber = pageNumber;
      this.pageSize = pageSize;
      this.totalElements = totalElements;
    }
  }
}
