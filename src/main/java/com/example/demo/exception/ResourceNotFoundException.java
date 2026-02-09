package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  /**
   * Creates a ResourceNotFoundException with entity name, field name, and field value. Example: new
   * ResourceNotFoundException("User", "id", 123) produces: "User not found with id: '123'"
   */
  public ResourceNotFoundException(String entityName, String fieldName, Object fieldValue) {
    super(String.format("%s not found with %s: '%s'", entityName, fieldName, fieldValue));
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
