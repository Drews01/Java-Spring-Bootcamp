package com.example.demo.dto;

/**
 * Lightweight DTO for branch dropdown in mobile apps. Contains only essential fields (id + name) to
 * minimize payload.
 */
public record BranchDropdownDTO(Long id, String name) {}
