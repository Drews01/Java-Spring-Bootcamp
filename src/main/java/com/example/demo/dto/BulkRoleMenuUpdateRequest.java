package com.example.demo.dto;

import java.util.List;

public record BulkRoleMenuUpdateRequest(Long roleId, List<Long> menuIds) {}
