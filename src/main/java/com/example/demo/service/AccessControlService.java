package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service("accessControl")
@RequiredArgsConstructor
public class AccessControlService {

    private final UserRepository userRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Transactional(readOnly = true)
    public boolean hasMenu(String menuCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Optimization: Admin has access to everything
        if (roleNames.contains("ADMIN")) {
            return true;
        }

        return roleMenuRepository.existsByRole_NameInAndMenu_Code(roleNames, menuCode);
    }
}
