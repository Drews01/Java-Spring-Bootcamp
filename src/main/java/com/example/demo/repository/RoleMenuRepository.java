package com.example.demo.repository;

import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.RoleMenuId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, RoleMenuId> {

  @Query(
      "SELECT rm FROM RoleMenu rm WHERE rm.roleId = :roleId AND (rm.deleted = false OR rm.deleted IS NULL)")
  List<RoleMenu> findByRoleId(@Param("roleId") Long roleId);

  @Query(
      "SELECT rm FROM RoleMenu rm WHERE rm.menuId = :menuId AND (rm.deleted = false OR rm.deleted IS NULL)")
  List<RoleMenu> findByMenuId(@Param("menuId") Long menuId);

  @Query(
      "SELECT rm FROM RoleMenu rm JOIN rm.role r WHERE r.name IN :roleNames AND (rm.deleted = false OR rm.deleted IS NULL)")
  List<RoleMenu> findByRole_NameIn(@Param("roleNames") java.util.Collection<String> roleNames);

  @Query(
      "SELECT COUNT(rm) > 0 FROM RoleMenu rm JOIN rm.role r JOIN rm.menu m WHERE r.name IN :roleNames AND m.code = :menuCode AND (rm.deleted = false OR rm.deleted IS NULL)")
  boolean existsByRole_NameInAndMenu_Code(
      @Param("roleNames") java.util.Collection<String> roleNames,
      @Param("menuCode") String menuCode);
}
