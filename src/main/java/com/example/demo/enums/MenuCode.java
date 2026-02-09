package com.example.demo.enums;

import lombok.Getter;

/**
 * Enum for menu codes with their respective categories. Used for RBAC (Role-Based Access Control)
 * to categorize menu permissions.
 */
@Getter
public enum MenuCode {

  // Admin Module
  ADMIN_DASHBOARD("Admin Module"),
  ADMIN_SYSTEM_LOGS("Admin Module"),

  // User Management
  USER_LIST("User Management"),
  USER_GET("User Management"),
  USER_CREATE("User Management"),
  USER_UPDATE("User Management"),
  USER_DELETE("User Management"),
  ADMIN_USER_LIST("User Management"),
  ADMIN_USER_CREATE("User Management"),
  ADMIN_USER_STATUS("User Management"),
  ADMIN_USER_ROLES("User Management"),

  // Role Management
  ROLE_LIST("Role Management"),
  ROLE_CREATE("Role Management"),
  ROLE_DELETE("Role Management"),

  // Menu Management
  MENU_LIST("Menu Management"),
  MENU_GET("Menu Management"),
  MENU_CREATE("Menu Management"),
  MENU_UPDATE("Menu Management"),
  MENU_DELETE("Menu Management"),

  // Loan Workflow
  LOAN_SUBMIT("Loan Workflow"),
  LOAN_ACTION("Loan Workflow"),
  LOAN_ALLOWED_ACTIONS("Loan Workflow"),
  LOAN_QUEUE_MARKETING("Loan Workflow"),
  LOAN_QUEUE_BRANCH_MANAGER("Loan Workflow"),
  LOAN_QUEUE_BACK_OFFICE("Loan Workflow"),
  LOAN_REVIEW("Loan Workflow"),
  LOAN_APPROVE("Loan Workflow"),
  LOAN_REJECT("Loan Workflow"),
  LOAN_DISBURSE("Loan Workflow"),

  // Loan Application
  LOAN_APP_CREATE("Loan Application"),
  LOAN_APP_GET("Loan Application"),
  LOAN_APP_BY_USER("Loan Application"),
  LOAN_APP_BY_STATUS("Loan Application"),
  LOAN_APP_LIST("Loan Application"),
  LOAN_APP_UPDATE("Loan Application"),
  LOAN_APP_DELETE("Loan Application"),

  // Loan History
  LOAN_HISTORY_CREATE("Loan History"),
  LOAN_HISTORY_GET("Loan History"),
  LOAN_HISTORY_BY_LOAN("Loan History"),
  LOAN_HISTORY_LIST("Loan History"),
  LOAN_HISTORY_DELETE("Loan History"),

  // Product Management
  PRODUCT_CREATE("Product Management"),
  PRODUCT_LIST("Product Management"),
  PRODUCT_ACTIVE("Product Management"),
  PRODUCT_BY_CODE("Product Management"),
  PRODUCT_UPDATE_STATUS("Product Management"),
  PRODUCT_DELETE("Product Management"),

  // User Product
  USER_PRODUCT_CREATE("User Product"),
  USER_PRODUCT_GET("User Product"),
  USER_PRODUCT_BY_USER("User Product"),
  USER_PRODUCT_ACTIVE("User Product"),
  USER_PRODUCT_LIST("User Product"),
  USER_PRODUCT_UPDATE("User Product"),
  USER_PRODUCT_DELETE("User Product"),

  // User Profile
  PROFILE_CREATE("User Profile"),
  PROFILE_ME("User Profile"),
  PROFILE_LIST("User Profile"),
  PROFILE_UPDATE("User Profile"),
  PROFILE_DELETE("User Profile"),

  // Notification
  NOTIFICATION_CREATE("Notification"),
  NOTIFICATION_GET("Notification"),
  NOTIFICATION_BY_USER("Notification"),
  NOTIFICATION_UNREAD("Notification"),
  NOTIFICATION_UNREAD_COUNT("Notification"),
  NOTIFICATION_LIST("Notification"),
  NOTIFICATION_MARK_READ("Notification"),
  NOTIFICATION_DELETE("Notification"),

  // Dashboards
  STAFF_DASHBOARD("Dashboards"),
  STAFF_QUEUE("Dashboards"),

  // RBAC Management
  RBAC_ROLES_LIST("RBAC Management"),
  RBAC_ROLE_ACCESS("RBAC Management"),
  RBAC_CATEGORIES("RBAC Management");

  private final String category;

  MenuCode(String category) {
    this.category = category;
  }

  /**
   * Gets the category for a given menu code string.
   *
   * @param code the menu code string
   * @return the category name, or "Other" if not found
   */
  public static String getCategory(String code) {
    if (code == null) {
      return "Other";
    }
    try {
      return valueOf(code).getCategory();
    } catch (IllegalArgumentException e) {
      return "Other";
    }
  }
}
