package com.example.demo.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Standard API Response wrapper untuk semua endpoint Memberikan format response yang konsisten */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Hanya include field yang tidak null
public class ApiResponse<T> {

  /** Status keberhasilan request (true/false) */
  private Boolean success;

  /** Pesan untuk user (success message atau error message) */
  private String message;

  /** Data response (bisa berupa object, list, atau null) */
  private T data;

  /** Error details (hanya muncul jika ada error) */
  private ErrorDetails error;

  /** Timestamp response */
  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();

  /** HTTP status code */
  private Integer statusCode;

  // ========== Helper Methods untuk Success Response ==========

  /** Success response dengan data */
  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .statusCode(200)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /** Success response dengan data (default message) */
  public static <T> ApiResponse<T> success(T data) {
    return success(data, "Request successful");
  }

  /** Success response untuk create (201 Created) */
  public static <T> ApiResponse<T> created(T data, String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .statusCode(201)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /** Success response tanpa data (untuk delete, update, dll) */
  public static <T> ApiResponse<T> success(String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .statusCode(200)
        .timestamp(LocalDateTime.now())
        .build();
  }

  // ========== Helper Methods untuk Error Response ==========

  /** Error response dengan details */
  public static <T> ApiResponse<T> error(
      String message, ErrorDetails errorDetails, Integer statusCode) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .error(errorDetails)
        .statusCode(statusCode)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /** Error response sederhana */
  public static <T> ApiResponse<T> error(String message, Integer statusCode) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .statusCode(statusCode)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /** Bad Request (400) */
  public static <T> ApiResponse<T> badRequest(String message) {
    return error(message, 400);
  }

  /** Not Found (404) */
  public static <T> ApiResponse<T> notFound(String message) {
    return error(message, 404);
  }

  /** Internal Server Error (500) */
  public static <T> ApiResponse<T> serverError(String message) {
    return error(message, 500);
  }
}
