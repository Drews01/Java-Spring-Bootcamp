package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkRoleMenuUpdateRequest(
    @NotNull(message = "roleId is required") Long roleId,
    @NotEmpty(message = "menuIds cannot be empty") List<Long> menuIds) {}
