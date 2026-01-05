package com.example.demo.repository;

import com.example.demo.entity.Menu;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

  Optional<Menu> findByName(String name);

  Optional<Menu> findByCode(String code);
}
