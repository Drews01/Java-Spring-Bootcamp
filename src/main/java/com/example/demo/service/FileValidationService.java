package com.example.demo.service;

import java.io.IOException;
import java.util.List;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidationService {

  private static final List<String> ALLOWED_MIME_TYPES =
      List.of("image/jpeg", "image/png", "image/gif", "application/pdf");

  private final Tika tika = new Tika();

  public void validateFile(MultipartFile file) {
    try {
      if (file.isEmpty()) {
        throw new RuntimeException("File is empty");
      }

      String detectedType = tika.detect(file.getInputStream());
      if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
        throw new RuntimeException(
            "Invalid file type. Detected: " + detectedType + ". Allowed: " + ALLOWED_MIME_TYPES);
      }

      // Consistency check (optional but recommended)
      String contentType = file.getContentType();
      if (contentType != null && !detectedType.equals(contentType)) {
        // Log warning or throw error depending on strictness
        // creating a stricter check here
        if (!isCompatible(detectedType, contentType)) {
          throw new RuntimeException(
              "File content type mismatch. Header: " + contentType + ", Detected: " + detectedType);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to validate file", e);
    }
  }

  private boolean isCompatible(String detected, String header) {
    // Simple compatibility check, can be expanded
    if (detected.equals(header)) return true;
    // Handle known browser quirks if necessary, e.g. jpg vs jpeg
    return false;
  }
}
