package com.example.demo.repository;

import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.RoleMenuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, RoleMenuId> {

    List<RoleMenu> findByRoleId(Long roleId);

    List<RoleMenu> findByMenuId(Long menuId);
}
