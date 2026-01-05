package com.example.demo.base;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class to create {@link ResponseEntity} instances that wrap the standardized {@link
 * ApiResponse}. Consolidates common response patterns so controllers stay lean and focused on
 * business logic.
 */
public final class ResponseUtil {

  private ResponseUtil() {
    // Utility class
  }

  /** 200 OK dengan payload dan pesan khusus. */
  public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
    return ResponseEntity.ok(ApiResponse.success(data, message));
  }

  /** 200 OK dengan payload dan pesan default. */
  public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  /** 200 OK tanpa payload, hanya pesan. */
  public static ResponseEntity<ApiResponse<Void>> okMessage(String message) {
    return ResponseEntity.ok(ApiResponse.success(message));
  }

  /** 201 Created dengan payload dan pesan khusus. */
  public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, message));
  }

  /** 201 Created dengan payload dan pesan default. */
  public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(data, "Resource created successfully"));
  }

  /** Response success generik dengan status custom. */
  public static <T> ResponseEntity<ApiResponse<T>> success(
      HttpStatus status, T data, String message) {
    return ResponseEntity.status(status)
        .body(
            ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(status.value())
                .build());
  }

  /** Error response generik dengan detail tambahan. */
  public static <T> ResponseEntity<ApiResponse<T>> error(
      HttpStatus status, String message, ErrorDetails details) {
    return ResponseEntity.status(status).body(ApiResponse.error(message, details, status.value()));
  }

  /** Error response generik tanpa detail tambahan. */
  public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(ApiResponse.error(message, status.value()));
  }

  /** 400 Bad Request helper. */
  public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
    return error(HttpStatus.BAD_REQUEST, message);
  }

  /** 404 Not Found helper. */
  public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
    return error(HttpStatus.NOT_FOUND, message);
  }

  /** 500 Internal Server Error helper. */
  public static <T> ResponseEntity<ApiResponse<T>> serverError(String message) {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }
}
