package com.example.demo.exception;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ErrorDetails;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler Handles exceptions across all controllers and returns consistent
 * ApiResponse format
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Handle validation errors */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              fieldErrors.put(fieldName, errorMessage);
            });

    ErrorDetails errorDetails =
        ErrorDetails.builder().errorCode("VALIDATION_ERROR").fieldErrors(fieldErrors).build();

    ApiResponse<Void> response =
        ApiResponse.error("Validation failed", errorDetails, HttpStatus.BAD_REQUEST.value());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** Handle bad credentials (wrong password) */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
    ErrorDetails errorDetails = ErrorDetails.builder().errorCode("INVALID_CREDENTIALS").build();

    ApiResponse<Void> response =
        ApiResponse.error(
            "Invalid username or password", errorDetails, HttpStatus.UNAUTHORIZED.value());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /** Handle user not found */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UsernameNotFoundException ex) {
    ErrorDetails errorDetails = ErrorDetails.builder().errorCode("USER_NOT_FOUND").build();

    ApiResponse<Void> response =
        ApiResponse.error(ex.getMessage(), errorDetails, HttpStatus.NOT_FOUND.value());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /** Handle duplicate username/email */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    ErrorDetails errorDetails =
        ErrorDetails.builder()
            .errorCode("DUPLICATE_ENTRY")
            .additionalInfo(Map.of("rootCause", ex.getMostSpecificCause().getMessage()))
            .build();

    ApiResponse<Void> response =
        ApiResponse.error(
            "Data integrity violation: " + ex.getMostSpecificCause().getMessage(),
            errorDetails,
            HttpStatus.CONFLICT.value());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  /** Handle illegal argument (custom validation) */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    ErrorDetails errorDetails = ErrorDetails.builder().errorCode("INVALID_ARGUMENT").build();

    ApiResponse<Void> response =
        ApiResponse.error(ex.getMessage(), errorDetails, HttpStatus.BAD_REQUEST.value());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** Handle generic exceptions */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    ErrorDetails errorDetails = ErrorDetails.builder().errorCode("INTERNAL_ERROR").build();

    ApiResponse<Void> response =
        ApiResponse.error(
            "An unexpected error occurred: " + ex.getMessage(),
            errorDetails,
            HttpStatus.INTERNAL_SERVER_ERROR.value());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
