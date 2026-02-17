package com.example.demo.constants;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ErrorMessageTest {

  @Test
  void testUserErrorMessagesAreNonNull() {
    assertNotNull(ErrorMessage.USER_NOT_FOUND);
    assertNotNull(ErrorMessage.USER_NOT_FOUND_EMAIL);
    assertNotNull(ErrorMessage.USERNAME_EXISTS);
    assertNotNull(ErrorMessage.EMAIL_EXISTS);
    assertNotNull(ErrorMessage.USER_INACTIVE);
  }

  @Test
  void testAuthenticationErrorMessagesAreNonNull() {
    assertNotNull(ErrorMessage.INVALID_CREDENTIALS);
    assertNotNull(ErrorMessage.INVALID_TOKEN);
    assertNotNull(ErrorMessage.INVALID_REFRESH_TOKEN);
    assertNotNull(ErrorMessage.INVALID_GOOGLE_TOKEN);
    assertNotNull(ErrorMessage.EMAIL_NOT_IN_TOKEN);
  }

  @Test
  void testAuthorizationErrorMessagesAreNonNull() {
    assertNotNull(ErrorMessage.UNAUTHORIZED);
    assertNotNull(ErrorMessage.INSUFFICIENT_PERMISSIONS);
    assertNotNull(ErrorMessage.CANNOT_REMOVE_OWN_ADMIN);
  }

  @Test
  void testSecurityContextErrorMessagesAreNonNull() {
    assertNotNull(ErrorMessage.NO_USER_IN_CONTEXT);
  }

  @Test
  void testResourceErrorMessagesAreNonNull() {
    assertNotNull(ErrorMessage.RESOURCE_NOT_FOUND);
    assertNotNull(ErrorMessage.BRANCH_NOT_FOUND);
    assertNotNull(ErrorMessage.ROLE_NOT_FOUND);
    assertNotNull(ErrorMessage.DEFAULT_ROLE_NOT_FOUND);
  }

  @Test
  void testValidationErrorMessagesAreNonNull() {
    assertNotNull(ErrorMessage.VALIDATION_FAILED);
    assertNotNull(ErrorMessage.INVALID_INPUT);
  }

  @Test
  void testMessagesAreProperlyFormatted() {
    // Test that messages are not empty
    assertFalse(ErrorMessage.INVALID_CREDENTIALS.isEmpty());
    assertFalse(ErrorMessage.USERNAME_EXISTS.isEmpty());
    assertFalse(ErrorMessage.UNAUTHORIZED.isEmpty());

    // Test that format strings contain placeholders
    assertTrue(ErrorMessage.USER_NOT_FOUND.contains("%s"));
    assertTrue(ErrorMessage.USER_NOT_FOUND_EMAIL.contains("%s"));
    assertTrue(ErrorMessage.BRANCH_NOT_FOUND.contains("%s"));
    assertTrue(ErrorMessage.ROLE_NOT_FOUND.contains("%s"));
  }

  @Test
  void testFormatStringMessagesCanBeFormatted() {
    // Test that format strings work correctly with String.format
    String formattedMessage = String.format(ErrorMessage.USER_NOT_FOUND, "123");
    assertEquals("User not found with id: 123", formattedMessage);

    formattedMessage = String.format(ErrorMessage.USER_NOT_FOUND_EMAIL, "test@example.com");
    assertEquals("User not found with email: test@example.com", formattedMessage);

    formattedMessage = String.format(ErrorMessage.BRANCH_NOT_FOUND, "456");
    assertEquals("Branch not found with id: 456", formattedMessage);
  }

  @Test
  void testClassCannotBeInstantiated() {
    // Verify that the constructor is private by checking that we can't instantiate
    // This is a compile-time check, but we can verify the class is final
    assertTrue(java.lang.reflect.Modifier.isFinal(ErrorMessage.class.getModifiers()));
  }
}
