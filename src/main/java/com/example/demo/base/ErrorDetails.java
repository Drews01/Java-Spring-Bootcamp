package com.example.demo.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Error details untuk response error
 * Memberikan informasi detail tentang error yang terjadi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    /**
     * Error code (bisa custom code seperti "USER_NOT_FOUND", "VALIDATION_ERROR",
     * dll)
     */
    private String errorCode;

    /**
     * Field yang error (untuk validation error)
     * Contoh: {"email": "Email format tidak valid", "password": "Password minimal 8
     * karakter"}
     */
    private Map<String, String> fieldErrors;

    /**
     * List error messages (untuk multiple errors)
     */
    private List<String> errors;

    /**
     * Stack trace (hanya untuk development, jangan tampilkan di production)
     */
    private String stackTrace;

    /**
     * Additional details
     */
    private Map<String, Object> additionalInfo;
}
