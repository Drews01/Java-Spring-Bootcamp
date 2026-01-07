package com.example.demo.dto;

public record RoleAccessSummaryDTO(
    Long roleId, String roleName, int totalMenus, int assignedMenus) {}
