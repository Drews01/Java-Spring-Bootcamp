package com.example.demo.service;

import com.example.demo.dto.RoleDTO;
import com.example.demo.entity.Role;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoleRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  @Transactional
  public RoleDTO createRole(Role role) {
    Role saved = roleRepository.save(role);
    return RoleDTO.fromEntity(saved);
  }

  public List<RoleDTO> getAllRoles() {
    return roleRepository.findByDeletedFalse().stream()
        .map(RoleDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteRole(Long id) {
    Role role =
        roleRepository
            .findById(id)
            .filter(r -> r.getDeleted() == null || !r.getDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    role.setDeleted(true);
    role.setIsActive(false);
    roleRepository.save(role);
  }
}
