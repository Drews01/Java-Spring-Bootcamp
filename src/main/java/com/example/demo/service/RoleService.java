package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findByDeletedFalse();
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDeleted() == null || !r.getDeleted())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        role.setDeleted(true);
        role.setIsActive(false);
        roleRepository.save(role);
    }
}
