package com.example.demo.dto;

import java.util.List;

public record RoleAccessDTO(Long roleId, String roleName, List<MenuGroupDTO> menuGroups) {}
