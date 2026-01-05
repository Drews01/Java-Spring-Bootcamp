package com.example.demo.security;

import com.example.demo.entity.User;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<String> roles =
        user.getRoles().stream().map(role -> "ROLE_" + role.getName()).collect(Collectors.toSet());
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return Boolean.TRUE.equals(user.getIsActive());
  }

  @Override
  public boolean isAccountNonLocked() {
    return Boolean.TRUE.equals(user.getIsActive());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return Boolean.TRUE.equals(user.getIsActive());
  }

  @Override
  public boolean isEnabled() {
    return Boolean.TRUE.equals(user.getIsActive());
  }

  public String getEmail() {
    return user.getEmail();
  }

  public Set<String> getRoleNames() {
    return user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
  }

  public Long getId() {
    return user.getId();
  }

  public User getUser() {
    return user;
  }
}
