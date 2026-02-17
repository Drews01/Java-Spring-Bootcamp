package com.example.demo.constants;

public final class ErrorMessage {
  private ErrorMessage() {} // Prevent instantiation

  // User errors
  public static final String USER_NOT_FOUND = "User not found with id: %s";
  public static final String USER_NOT_FOUND_EMAIL = "User not found with email: %s";
  public static final String USERNAME_EXISTS = "Username already exists";
  public static final String EMAIL_EXISTS = "Email already exists";
  public static final String USER_INACTIVE = "User is inactive";

  // Authentication errors
  public static final String INVALID_CREDENTIALS = "Invalid username or password";
  public static final String INVALID_TOKEN = "Invalid or expired token";
  public static final String INVALID_REFRESH_TOKEN = "Invalid or expired refresh token";
  public static final String INVALID_GOOGLE_TOKEN = "Invalid Google ID Token";
  public static final String EMAIL_NOT_IN_TOKEN = "Email not found in ID Token";

  // Authorization errors
  public static final String UNAUTHORIZED = "Unauthorized access";
  public static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions";
  public static final String CANNOT_REMOVE_OWN_ADMIN = "Cannot remove your own admin role";

  // Security context errors
  public static final String NO_USER_IN_CONTEXT =
      "Unable to get current user ID from security context";

  // Resource errors
  public static final String RESOURCE_NOT_FOUND = "%s not found with %s: %s";
  public static final String BRANCH_NOT_FOUND = "Branch not found with id: %s";
  public static final String ROLE_NOT_FOUND = "Role not found: %s";
  public static final String DEFAULT_ROLE_NOT_FOUND = "Default USER role not found";

  // Validation errors
  public static final String VALIDATION_FAILED = "Validation failed";
  public static final String INVALID_INPUT = "Invalid input provided";
}
