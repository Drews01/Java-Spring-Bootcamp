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
    Menu loanReject =
        findOrCreateMenu("LOAN_REJECT", "Reject Loan Request", "/api/loan-workflow/action");

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

    // BRANCH MANAGER
    mapRoleToMenu(branchManagerRole, loanApproveMenu);
    mapRoleToMenu(branchManagerRole, loanReject);
    mapRoleToMenu(branchManagerRole, branchRead);
    mapRoleToMenu(branchManagerRole, managerModule);

    // BACK OFFICE
    mapRoleToMenu(backOfficeRole, loanDisburseMenu);
    mapRoleToMenu(backOfficeRole, backOfficeModule);

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

    // Initialize sample loan products if they don't exist
    productRepository
        .findByCode("LOAN-PERSONAL-001")
        .orElseGet(
            () -> {
              Product product =
                  Product.builder()
                      .code("LOAN-PERSONAL-001")
                      .name("Personal Loan - Standard")
                      .interestRate(8.5)
                      .interestRateType("FIXED")
                      .minAmount(5000000.0)
                      .maxAmount(50000000.0)
                      .minTenureMonths(6)
                      .maxTenureMonths(36)
                      .isActive(true)
                      .build();
              return productRepository.save(product);
            });

    productRepository
        .findByCode("LOAN-MORTGAGE-001")
        .orElseGet(
            () -> {
              Product product =
                  Product.builder()
                      .code("LOAN-MORTGAGE-001")
                      .name("Home Mortgage Loan")
                      .interestRate(6.75)
                      .interestRateType("FLOATING")
                      .minAmount(100000000.0)
                      .maxAmount(2000000000.0)
                      .minTenureMonths(60)
                      .maxTenureMonths(240)
                      .isActive(true)
                      .build();
              return productRepository.save(product);
            });

    productRepository
        .findByCode("LOAN-VEHICLE-001")
        .orElseGet(
            () -> {
              Product product =
                  Product.builder()
                      .code("LOAN-VEHICLE-001")
                      .name("Vehicle Loan - Auto")
                      .interestRate(7.25)
                      .interestRateType("FIXED")
                      .minAmount(20000000.0)
                      .maxAmount(500000000.0)
                      .minTenureMonths(12)
                      .maxTenureMonths(60)
                      .isActive(true)
                      .build();
              return productRepository.save(product);
            });

    System.out.println("✓ Data initialization completed!");
    System.out.println("✓ Roles created: ADMIN, USER, BACK_OFFICE, BRANCH_MANAGER, MARKETING");
    System.out.println("✓ Menus & RBAC seeded");
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
