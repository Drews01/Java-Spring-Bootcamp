package com.example.demo.dto;

import java.util.List;

/**
 * DTO for the unified staff dashboard response. Contains role-specific information for Marketing,
 * Branch Manager, and Back Office users.
 */
public record StaffDashboardDTO(
    String username,
    String primaryRole,
    List<String> allRoles,
    String queueName,
    List<String> allowedActions,
    String welcomeMessage) {}
