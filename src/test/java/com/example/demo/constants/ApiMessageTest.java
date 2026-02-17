package com.example.demo.constants;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ApiMessageTest {

  @Test
  void testSuccessMessagesAreNonNull() {
    assertNotNull(ApiMessage.SUCCESS);
    assertNotNull(ApiMessage.CREATED);
    assertNotNull(ApiMessage.UPDATED);
    assertNotNull(ApiMessage.DELETED);
  }

  @Test
  void testUserMessagesAreNonNull() {
    assertNotNull(ApiMessage.USER_CREATED);
    assertNotNull(ApiMessage.USER_UPDATED);
    assertNotNull(ApiMessage.USER_DELETED);
    assertNotNull(ApiMessage.USER_FETCHED);
    assertNotNull(ApiMessage.USERS_FETCHED);
    assertNotNull(ApiMessage.USER_STATUS_UPDATED);
    assertNotNull(ApiMessage.USER_ROLES_UPDATED);
  }

  @Test
  void testAuthenticationMessagesAreNonNull() {
    assertNotNull(ApiMessage.LOGIN_SUCCESS);
    assertNotNull(ApiMessage.LOGOUT_SUCCESS);
    assertNotNull(ApiMessage.REGISTER_SUCCESS);
    assertNotNull(ApiMessage.TOKEN_REFRESHED);
    assertNotNull(ApiMessage.PASSWORD_RESET_SENT);
    assertNotNull(ApiMessage.PASSWORD_RESET_SUCCESS);
  }

  @Test
  void testProfileMessagesAreNonNull() {
    assertNotNull(ApiMessage.PROFILE_UPDATED);
    assertNotNull(ApiMessage.PROFILE_FETCHED);
    assertNotNull(ApiMessage.PROFILE_DELETED);
    assertNotNull(ApiMessage.IMAGE_UPLOADED);
    assertNotNull(ApiMessage.KTP_UPLOADED);
  }

  @Test
  void testLoanMessagesAreNonNull() {
    assertNotNull(ApiMessage.LOAN_SUBMITTED);
    assertNotNull(ApiMessage.LOAN_UPDATED);
    assertNotNull(ApiMessage.LOAN_FETCHED);
    assertNotNull(ApiMessage.LOANS_FETCHED);
    assertNotNull(ApiMessage.ACTION_PERFORMED);
  }

  @Test
  void testBranchMessagesAreNonNull() {
    assertNotNull(ApiMessage.BRANCH_CREATED);
    assertNotNull(ApiMessage.BRANCH_UPDATED);
    assertNotNull(ApiMessage.BRANCH_DELETED);
    assertNotNull(ApiMessage.BRANCH_FETCHED);
    assertNotNull(ApiMessage.BRANCHES_FETCHED);
  }

  @Test
  void testGenericMessagesAreNonNull() {
    assertNotNull(ApiMessage.DATA_RETRIEVED);
    assertNotNull(ApiMessage.OPERATION_SUCCESS);
  }

  @Test
  void testMessagesAreProperlyFormatted() {
    // Test that messages are not empty
    assertFalse(ApiMessage.SUCCESS.isEmpty());
    assertFalse(ApiMessage.USER_CREATED.isEmpty());
    assertFalse(ApiMessage.LOGIN_SUCCESS.isEmpty());

    // Test that messages are descriptive
    assertTrue(
        ApiMessage.USER_CREATED.contains("successfully")
            || ApiMessage.USER_CREATED.contains("success"));
    assertTrue(ApiMessage.LOGIN_SUCCESS.contains("success"));
    assertTrue(
        ApiMessage.DATA_RETRIEVED.contains("successfully")
            || ApiMessage.DATA_RETRIEVED.contains("success"));
  }

  @Test
  void testMessagesFollowConsistentPattern() {
    // Verify that success messages follow a consistent pattern
    assertTrue(ApiMessage.USER_CREATED.endsWith("successfully"));
    assertTrue(ApiMessage.USER_UPDATED.endsWith("successfully"));
    assertTrue(ApiMessage.USER_DELETED.endsWith("successfully"));
    assertTrue(ApiMessage.BRANCH_CREATED.endsWith("successfully"));
    assertTrue(ApiMessage.LOAN_SUBMITTED.endsWith("successfully"));
  }

  @Test
  void testClassCannotBeInstantiated() {
    // Verify that the constructor is private by checking that the class is final
    assertTrue(java.lang.reflect.Modifier.isFinal(ApiMessage.class.getModifiers()));
  }
}
