package com.example.demo.service;

import com.example.demo.dto.MenuDTO;
import com.example.demo.entity.Menu;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.MenuRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

  private final MenuRepository menuRepository;

  @Transactional
  public MenuDTO createMenu(MenuDTO dto) {
    Menu menu =
        Menu.builder()
            .code(dto.getCode())
            .name(dto.getName())
            .urlPattern(dto.getUrlPattern())
            .build();

    Menu saved = menuRepository.save(menu);
    return convertToDTO(saved);
  }

  @Transactional(readOnly = true)
  public MenuDTO getMenu(Long menuId) {
    Menu menu =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuId));
    return convertToDTO(menu);
  }

  @Transactional(readOnly = true)
  public List<MenuDTO> getAllMenus() {
    return menuRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Transactional
  public MenuDTO updateMenu(Long menuId, MenuDTO dto) {
    Menu menu =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuId));

    menu.setCode(dto.getCode());
    menu.setName(dto.getName());
    menu.setUrlPattern(dto.getUrlPattern());

    Menu updated = menuRepository.save(menu);
    return convertToDTO(updated);
  }

  @Transactional
  public void deleteMenu(Long menuId) {
    menuRepository.deleteById(menuId);
  }

  private MenuDTO convertToDTO(Menu menu) {
    return MenuDTO.builder()
        .menuId(menu.getMenuId())
        .code(menu.getCode())
        .name(menu.getName())
        .urlPattern(menu.getUrlPattern())
        .build();
  }
}
