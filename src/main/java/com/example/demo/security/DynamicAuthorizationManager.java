package com.example.demo.security;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class DynamicAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final UserRepository userRepository;
  private final RoleMenuRepository roleMenuRepository;
  private final MenuRepository menuRepository;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  public AuthorizationDecision authorize(
      Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
    Authentication auth = authentication.get();
    if (auth == null || !auth.isAuthenticated()) {
      return new AuthorizationDecision(false);
    }

    // Get both servlet path and request URI to be robust
    String servletPath = context.getRequest().getServletPath();
    String requestUri = context.getRequest().getRequestURI();
    String username = auth.getName();

    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) {
      return new AuthorizationDecision(false);
    }

    Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    // Admin has access to everything
    if (roleNames.contains("ADMIN")) {
      return new AuthorizationDecision(true);
    }

    // Safety check: if user has no roles, they definitely don't have menu access
    if (roleNames.isEmpty()) {
      return new AuthorizationDecision(false);
    }

    // 5. Pre-fetch all allowed Menu IDs for this user's roles (Avoid N+1)
    Set<Long> allowedMenuIds =
        roleMenuRepository.findByRole_NameIn(roleNames).stream()
            .map(com.example.demo.entity.RoleMenu::getMenuId)
            .collect(Collectors.toSet());

    // Find all menus with URL patterns
    List<com.example.demo.entity.Menu> menusWithPatterns =
        menuRepository.findAll().stream()
            .filter(m -> m.getUrlPattern() != null && !m.getUrlPattern().isEmpty())
            .collect(Collectors.toList());

    boolean matchedAnyPattern = false;
    boolean authorizedByAnyMatch = false;

    // Check against both servletPath and requestUri
    for (com.example.demo.entity.Menu menu : menusWithPatterns) {
      String pattern = menu.getUrlPattern();

      if (pathMatcher.match(pattern, servletPath) || pathMatcher.match(pattern, requestUri)) {
        matchedAnyPattern = true;

        if (allowedMenuIds.contains(menu.getMenuId())) {
          authorizedByAnyMatch = true;
          break; // Found an authorized match, we can stop
        }
      }
    }

    if (matchedAnyPattern) {
      return new AuthorizationDecision(authorizedByAnyMatch);
    }

    // If no patterns matched, default to requiring authentication
    return new AuthorizationDecision(true);
  }
}
