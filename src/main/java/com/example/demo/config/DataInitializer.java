package com.example.demo.config;

import com.example.demo.entity.Menu;
import com.example.demo.entity.Product;
import com.example.demo.entity.Role;
import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.User;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final MenuRepository menuRepository;
  private final RoleMenuRepository roleMenuRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    cleanupDuplicateRoles();

    Role adminRole = findOrCreateRole("ADMIN");
    Role userRole = findOrCreateRole("USER");
    Role backOfficeRole = findOrCreateRole("BACK_OFFICE");
    Role branchManagerRole = findOrCreateRole("BRANCH_MANAGER");
    Role marketingRole = findOrCreateRole("MARKETING");

    // Initialize Menus
    Menu loanSubmitMenu =
        findOrCreateMenu("LOAN_SUBMIT", "Submit Loan", "/api/loan-workflow/submit");
    Menu loanReviewMenu =
        findOrCreateMenu("LOAN_REVIEW", "Review Loan", "/api/loan-workflow/queue/marketing");
    Menu loanApproveMenu =
        findOrCreateMenu("LOAN_APPROVE", "Approve Loan", "/api/loan-workflow/queue/branch-manager");
    Menu loanDisburseMenu =
        findOrCreateMenu("LOAN_DISBURSE", "Disburse Loan", "/api/loan-workflow/queue/back-office");

    // New RBAC Master Data Menus
    Menu userRead = findOrCreateMenu("USER_READ", "Read Users", "/api/users/**");
    Menu userCreate = findOrCreateMenu("USER_CREATE", "Create User", "/api/users");
    Menu userUpdate = findOrCreateMenu("USER_UPDATE", "Update User", "/api/users/**");
    Menu userDelete = findOrCreateMenu("USER_DELETE", "Delete User", "/api/users/**");

    Menu roleRead = findOrCreateMenu("ROLE_READ", "Read Roles", "/api/roles/**");
    Menu roleAssign = findOrCreateMenu("ROLE_ASSIGN", "Assign Role", "/api/roles/assign");
    Menu roleManage = findOrCreateMenu("ROLE_MANAGE", "Manage Roles", "/api/roles/**");

    Menu loanCreate =
        findOrCreateMenu("LOAN_CREATE", "Create Loan Request", "/api/loan-workflow/submit");
    // Renamed generic action menu to be shared across roles
    Menu loanAction =
        findOrCreateMenu("LOAN_ACTION", "Perform Loan Action", "/api/loan-workflow/action");

    Menu productRead = findOrCreateMenu("PRODUCT_READ", "Read Products", "/api/products/**");
    Menu productManage = findOrCreateMenu("PRODUCT_MANAGE", "Manage Products", "/api/products/**");

    Menu branchRead =
        findOrCreateMenu(
            "BRANCH_READ", "Branch Reports", "/api/loan-workflow/queue/branch-manager");

    Menu profileRead = findOrCreateMenu("PROFILE_READ", "Read Profile", "/api/user-profiles/**");
    Menu profileUpdate =
        findOrCreateMenu("PROFILE_UPDATE", "Update Profile", "/api/user-profiles/**");

    // Role-Specific Functional Modules
    Menu marketingModule =
        findOrCreateMenu("MARKETING_MODULE", "Marketing Dashboard", "/api/marketing/**");
    Menu managerModule =
        findOrCreateMenu("MANAGER_MODULE", "Branch Manager Dashboard", "/api/branch-manager/**");
    Menu backOfficeModule =
        findOrCreateMenu("BACKOFFICE_MODULE", "Back Office Dashboard", "/api/back-office/**");
    Menu adminModule = findOrCreateMenu("ADMIN_MODULE", "Admin Dashboard", "/api/admin/**");

    findOrCreateMenu("DASHBOARD", "User Dashboard", "/api/dashboard/**");

    // Initialize Role-Menu mappings
    // USER (Customer)
    mapRoleToMenu(userRole, loanCreate);
    mapRoleToMenu(userRole, productRead);
    mapRoleToMenu(userRole, profileRead);
    mapRoleToMenu(userRole, profileUpdate);
    mapRoleToMenu(userRole, loanSubmitMenu);

    // MARKETING
    mapRoleToMenu(marketingRole, loanReviewMenu);
    mapRoleToMenu(marketingRole, marketingModule);
    mapRoleToMenu(marketingRole, loanAction); // Added common action access

    // BRANCH MANAGER
    mapRoleToMenu(branchManagerRole, loanApproveMenu);
    mapRoleToMenu(branchManagerRole, loanAction); // Shared action access
    mapRoleToMenu(branchManagerRole, branchRead);
    mapRoleToMenu(branchManagerRole, managerModule);

    // BACK OFFICE
    mapRoleToMenu(backOfficeRole, loanDisburseMenu);
    mapRoleToMenu(backOfficeRole, backOfficeModule);
    mapRoleToMenu(backOfficeRole, loanAction); // Added common action access

    // ADMIN (Full Access)
    mapRoleToMenu(adminRole, adminModule);
    mapRoleToMenu(adminRole, userRead);
    mapRoleToMenu(adminRole, userCreate);
    mapRoleToMenu(adminRole, userUpdate);
    mapRoleToMenu(adminRole, userDelete);
    mapRoleToMenu(adminRole, roleRead);
    mapRoleToMenu(adminRole, roleAssign);
    mapRoleToMenu(adminRole, roleManage);
    mapRoleToMenu(adminRole, productManage);

    // Initialize test users
    createTestUser("admin", "admin@example.com", "admin123", adminRole, userRole);
    createTestUser("marketing", "marketing@example.com", "pass123", marketingRole);
    createTestUser("manager", "manager@example.com", "pass123", branchManagerRole);
    createTestUser("backoffice", "backoffice@example.com", "pass123", backOfficeRole);
    createTestUser("user", "user@example.com", "pass123", userRole);

    // Initialize tier-based loan products (Bronze, Silver, Gold)
    initializeTierProducts();

    System.out.println("✓ Data initialization completed!");
    System.out.println("✓ Roles created: ADMIN, USER, BACK_OFFICE, BRANCH_MANAGER, MARKETING");
    System.out.println("✓ Menus & RBAC seeded");
    System.out.println("✓ Tier products created: BRONZE, SILVER, GOLD");
  }

  private void initializeTierProducts() {
    // Bronze Tier - Entry level
    findOrCreateTierProduct(
        "TIER-BRONZE",
        "Bronze Loan Product",
        8.0, // 8% interest rate
        10000000.0, // Credit limit: 10 million
        15000000.0, // Upgrade threshold: 15 million paid
        1 // Tier order
        );

    // Silver Tier - Mid level
    findOrCreateTierProduct(
        "TIER-SILVER",
        "Silver Loan Product",
        7.0, // 7% interest rate
        25000000.0, // Credit limit: 25 million
        50000000.0, // Upgrade threshold: 50 million paid
        2 // Tier order
        );

    // Gold Tier - Top level
    findOrCreateTierProduct(
        "TIER-GOLD",
        "Gold Loan Product",
        6.0, // 6% interest rate
        50000000.0, // Credit limit: 50 million
        null, // No upgrade from Gold
        3 // Tier order
        );
  }

  private Product findOrCreateTierProduct(
      String code,
      String name,
      Double interestRate,
      Double creditLimit,
      Double upgradeThreshold,
      Integer tierOrder) {
    return productRepository
        .findByCode(code)
        .orElseGet(
            () -> {
              Product product =
                  Product.builder()
                      .code(code)
                      .name(name)
                      .interestRate(interestRate)
                      .interestRateType("FIXED")
                      .minAmount(1000000.0) // Min: 1 million
                      .maxAmount(creditLimit) // Max equals credit limit
                      .minTenureMonths(3)
                      .maxTenureMonths(36)
                      .tierOrder(tierOrder)
                      .creditLimit(creditLimit)
                      .upgradeThreshold(upgradeThreshold)
                      .isActive(true)
                      .build();
              System.out.println("✓ Created tier product: " + name);
              return productRepository.save(product);
            });
  }

  private Role findOrCreateRole(String name) {
    return roleRepository
        .findByName(name)
        .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
  }

  private Menu findOrCreateMenu(String code, String name, String urlPattern) {
    return menuRepository
        .findByCode(code)
        .orElseGet(
            () ->
                menuRepository.save(
                    Menu.builder().code(code).name(name).urlPattern(urlPattern).build()));
  }

  private void mapRoleToMenu(Role role, Menu menu) {
    if (!roleMenuRepository.existsById(
        new com.example.demo.entity.RoleMenuId(role.getId(), menu.getMenuId()))) {
      roleMenuRepository.save(
          RoleMenu.builder().roleId(role.getId()).menuId(menu.getMenuId()).build());
    }
  }

  private void cleanupDuplicateRoles() {
    mergeRoleIfExists("BACK OFFICE", "BACK_OFFICE");
    mergeRoleIfExists("BRANCH MANAGER", "BRANCH_MANAGER");
  }

  private void mergeRoleIfExists(String oldName, String newName) {
    roleRepository
        .findByName(oldName)
        .ifPresent(
            oldRole -> {
              Role newRole = findOrCreateRole(newName);

              // 1. Reassign users
              List<User> usersWithOldRole = userRepository.findByRoles_Name(oldName);
              for (User user : usersWithOldRole) {
                user.getRoles().remove(oldRole);
                user.getRoles().add(newRole);
                userRepository.save(user);
              }

              // 2. Reassign menu mappings
              List<RoleMenu> mappingsWithOldRole = roleMenuRepository.findByRoleId(oldRole.getId());
              for (RoleMenu mapping : mappingsWithOldRole) {
                if (!roleMenuRepository.existsById(
                    new com.example.demo.entity.RoleMenuId(newRole.getId(), mapping.getMenuId()))) {
                  roleMenuRepository.save(
                      RoleMenu.builder()
                          .roleId(newRole.getId())
                          .menuId(mapping.getMenuId())
                          .isActive(mapping.getIsActive())
                          .deleted(mapping.getDeleted())
                          .build());
                }
                roleMenuRepository.delete(mapping);
              }

              // 3. Delete old role (hard delete since it's a cleanup of duplicates)
              roleRepository.delete(oldRole);
              System.out.println(
                  "✓ Merged and removed duplicate role: " + oldName + " -> " + newName);
            });
  }

  private void createTestUser(String username, String email, String password, Role... roles) {
    userRepository
        .findByUsername(username)
        .orElseGet(
            () -> {
              Set<Role> roleSet = new HashSet<>();
              for (Role r : roles) {
                roleSet.add(r);
              }

              User user =
                  User.builder()
                      .username(username)
                      .email(email)
                      .password(passwordEncoder.encode(password))
                      .isActive(true)
                      .roles(roleSet)
                      .build();

              return userRepository.save(user);
            });
  }
}
