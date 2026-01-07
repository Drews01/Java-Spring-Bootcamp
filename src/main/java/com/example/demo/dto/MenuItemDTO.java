package com.example.demo.dto;

public record MenuItemDTO(
    Long menuId, String code, String name, String urlPattern, boolean isAssigned) {}
