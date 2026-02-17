package com.example.demo.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RoleNameTest {

  @Test
  void testEnumContainsAllExpectedValues() {
    // Verify all expected role names exist
    RoleName[] roles = RoleName.values();

    assertEquals(5, roles.length, "RoleName enum should contain exactly 5 values");

    // Verify each expected role exists
    assertNotNull(RoleName.valueOf("USER"));
    assertNotNull(RoleName.valueOf("ADMIN"));
    assertNotNull(RoleName.valueOf("MARKETING"));
    assertNotNull(RoleName.valueOf("BRANCH_MANAGER"));
    assertNotNull(RoleName.valueOf("BACK_OFFICE"));
  }

  @Test
  void testGetRoleNameReturnsCorrectValue() {
    assertEquals("USER", RoleName.USER.getRoleName());
    assertEquals("ADMIN", RoleName.ADMIN.getRoleName());
    assertEquals("MARKETING", RoleName.MARKETING.getRoleName());
    assertEquals("BRANCH_MANAGER", RoleName.BRANCH_MANAGER.getRoleName());
    assertEquals("BACK_OFFICE", RoleName.BACK_OFFICE.getRoleName());
  }

  @Test
  void testEnumNameMethod() {
    // Verify name() method returns correct string
    assertEquals("USER", RoleName.USER.name());
    assertEquals("ADMIN", RoleName.ADMIN.name());
  }

  @Test
  void testInvalidRoleNameThrowsException() {
    // Verify that invalid role name throws IllegalArgumentException
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RoleName.valueOf("INVALID_ROLE");
        });
  }
}
