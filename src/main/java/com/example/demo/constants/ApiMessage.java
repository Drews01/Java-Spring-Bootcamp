package com.example.demo.constants;

public final class ApiMessage {
  private ApiMessage() {} // Prevent instantiation

  // Success messages
  public static final String SUCCESS = "Request successful";
  public static final String CREATED = "Resource created successfully";
  public static final String UPDATED = "Resource updated successfully";
  public static final String DELETED = "Resource deleted successfully";

  // User messages
  public static final String USER_CREATED = "User created successfully";
  public static final String USER_UPDATED = "User updated successfully";
  public static final String USER_DELETED = "User deleted successfully";
  public static final String USER_FETCHED = "User fetched successfully";
  public static final String USERS_FETCHED = "Users fetched successfully";
  public static final String USER_STATUS_UPDATED = "User status updated successfully";
  public static final String USER_ROLES_UPDATED = "User roles updated successfully";

  // Authentication messages
  public static final String LOGIN_SUCCESS = "Login successful";
  public static final String LOGOUT_SUCCESS = "Logout successful";
  public static final String REGISTER_SUCCESS = "Registration successful";
  public static final String TOKEN_REFRESHED = "Token refreshed successfully";
  public static final String PASSWORD_RESET_SENT = "Password reset link sent to email";
  public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";

  // Profile messages
  public static final String PROFILE_UPDATED = "User profile updated successfully";
  public static final String PROFILE_FETCHED = "User profile retrieved successfully";
  public static final String PROFILE_DELETED = "User profile deleted successfully";
  public static final String IMAGE_UPLOADED = "Image uploaded successfully";
  public static final String KTP_UPLOADED = "KTP image uploaded successfully";

  // Loan messages
  public static final String LOAN_SUBMITTED = "Loan application submitted successfully";
  public static final String LOAN_UPDATED = "Loan application updated successfully";
  public static final String LOAN_FETCHED = "Loan application retrieved successfully";
  public static final String LOANS_FETCHED = "Loan applications retrieved successfully";
  public static final String ACTION_PERFORMED = "Action performed successfully";

  // Branch messages
  public static final String BRANCH_CREATED = "Branch created successfully";
  public static final String BRANCH_UPDATED = "Branch updated successfully";
  public static final String BRANCH_DELETED = "Branch deleted successfully";
  public static final String BRANCH_FETCHED = "Branch fetched successfully";
  public static final String BRANCHES_FETCHED = "Branches fetched successfully";

  // Generic messages
  public static final String DATA_RETRIEVED = "Data retrieved successfully";
  public static final String OPERATION_SUCCESS = "Operation completed successfully";
}
