package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesRequest(
    @NotEmpty(message = "roleNames cannot be empty") Set<String> roleNames) {}
