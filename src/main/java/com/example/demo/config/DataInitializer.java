package com.example.demo.config;

import com.example.demo.entity.Branch;
import com.example.demo.entity.Menu;
import com.example.demo.entity.Product;
import com.example.demo.entity.Role;
import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.User;
import com.example.demo.repository.BranchRepository;
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
  private final BranchRepository branchRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    cleanupDuplicateRoles();
    cleanupDeprecatedMenus();

    // ============================================================
    // ROLES INITIALIZATION
    // ============================================================
    Role adminRole = findOrCreateRole("ADMIN");
    Role userRole = findOrCreateRole("USER");
    Role backOfficeRole = findOrCreateRole("BACK_OFFICE");
    Role branchManagerRole = findOrCreateRole("BRANCH_MANAGER");
    Role marketingRole = findOrCreateRole("MARKETING");

    // ============================================================
    // MENUS INITIALIZATION - ALL ENDPOINTS
    // ============================================================

    // -------------------- ADMIN MODULE --------------------
    Menu adminDashboard =
        findOrCreateMenu("ADMIN_DASHBOARD", "Admin Dashboard", "/api/admin/dashboard");
    Menu adminSystemLogs =
        findOrCreateMenu("ADMIN_SYSTEM_LOGS", "Admin System Logs", "/api/admin/system-logs");

    // -------------------- USER MANAGEMENT (ADMIN) --------------------
    Menu userList = findOrCreateMenu("USER_LIST", "List All Users", "/api/users");
    Menu userGet = findOrCreateMenu("USER_GET", "Get User by ID", "/api/users/*");
    Menu userCreate = findOrCreateMenu("USER_CREATE", "Create User", "/api/users");
    Menu userUpdate = findOrCreateMenu("USER_UPDATE", "Update User", "/api/users/*");
    Menu userDelete = findOrCreateMenu("USER_DELETE", "Delete User", "/api/users/*");
    Menu adminUserList =
        findOrCreateMenu("ADMIN_USER_LIST", "Admin User List", "/api/users/admin/list");
    Menu adminUserCreate =
        findOrCreateMenu("ADMIN_USER_CREATE", "Admin Create User", "/api/users/admin/create");
    Menu adminUserStatus =
        findOrCreateMenu("ADMIN_USER_STATUS", "Admin User Status", "/api/users/admin/*/status");
    Menu adminUserRoles =
        findOrCreateMenu("ADMIN_USER_ROLES", "Admin User Roles", "/api/users/admin/*/roles");

    // -------------------- ROLE MANAGEMENT (ADMIN) --------------------
    Menu roleList = findOrCreateMenu("ROLE_LIST", "List All Roles", "/api/roles");
    Menu roleCreate = findOrCreateMenu("ROLE_CREATE", "Create Role", "/api/roles");
    Menu roleDelete = findOrCreateMenu("ROLE_DELETE", "Delete Role", "/api/roles/*");

    // -------------------- MENU MANAGEMENT (ADMIN) --------------------
    Menu menuList = findOrCreateMenu("MENU_LIST", "List All Menus", "/api/menus");
    Menu menuGet = findOrCreateMenu("MENU_GET", "Get Menu by ID", "/api/menus/*");
    Menu menuCreate = findOrCreateMenu("MENU_CREATE", "Create Menu", "/api/menus");
    Menu menuUpdate = findOrCreateMenu("MENU_UPDATE", "Update Menu", "/api/menus/*");
    Menu menuDelete = findOrCreateMenu("MENU_DELETE", "Delete Menu", "/api/menus/*");

    // -------------------- ROLE-MENU MANAGEMENT (ADMIN) --------------------
    Menu roleMenuAssign =
        findOrCreateMenu("ROLE_MENU_ASSIGN", "Assign Menu to Role", "/api/role-menus");
    Menu roleMenuGetByRole =
        findOrCreateMenu("ROLE_MENU_BY_ROLE", "Get Menus by Role", "/api/role-menus/role/*");
    Menu roleMenuGetByMenu =
        findOrCreateMenu("ROLE_MENU_BY_MENU", "Get Roles by Menu", "/api/role-menus/menu/*");
    Menu roleMenuRemove =
        findOrCreateMenu("ROLE_MENU_REMOVE", "Remove Menu from Role", "/api/role-menus");

    // -------------------- LOAN WORKFLOW --------------------
    Menu loanSubmit = findOrCreateMenu("LOAN_SUBMIT", "Submit Loan", "/api/loan-workflow/submit");
    Menu loanAction =
        findOrCreateMenu("LOAN_ACTION", "Perform Loan Action", "/api/loan-workflow/action");
    Menu loanAllowedActions =
        findOrCreateMenu(
            "LOAN_ALLOWED_ACTIONS", "Get Allowed Actions", "/api/loan-workflow/*/allowed-actions");
    Menu loanQueueMarketing =
        findOrCreateMenu(
            "LOAN_QUEUE_MARKETING", "Marketing Queue", "/api/loan-workflow/queue/marketing");
    Menu loanQueueBranchManager =
        findOrCreateMenu(
            "LOAN_QUEUE_BRANCH_MANAGER",
            "Branch Manager Queue",
            "/api/loan-workflow/queue/branch-manager");
    Menu loanQueueBackOffice =
        findOrCreateMenu(
            "LOAN_QUEUE_BACK_OFFICE", "Back Office Queue", "/api/loan-workflow/queue/back-office");

    // Permission menus for workflow actions (not URL-based, but permission-based)
    Menu loanReview =
        findOrCreateMenu("LOAN_REVIEW", "Review Loans (Marketing)", "/api/loan-workflow/action");
    Menu loanApprove =
        findOrCreateMenu(
            "LOAN_APPROVE", "Approve Loans (Branch Manager)", "/api/loan-workflow/action");
    Menu loanReject =
        findOrCreateMenu(
            "LOAN_REJECT", "Reject Loans (Branch Manager)", "/api/loan-workflow/action");
    Menu loanDisburse =
        findOrCreateMenu(
            "LOAN_DISBURSE", "Disburse Loans (Back Office)", "/api/loan-workflow/action");

    // -------------------- LOAN APPLICATION (ADMIN/STAFF) --------------------
    Menu loanAppCreate =
        findOrCreateMenu("LOAN_APP_CREATE", "Create Loan Application", "/api/loan-applications");
    Menu loanAppGet =
        findOrCreateMenu("LOAN_APP_GET", "Get Loan Application", "/api/loan-applications/*");
    Menu loanAppByUser =
        findOrCreateMenu(
            "LOAN_APP_BY_USER", "Get Loan Apps by User", "/api/loan-applications/user/*");
    Menu loanAppByStatus =
        findOrCreateMenu(
            "LOAN_APP_BY_STATUS", "Get Loan Apps by Status", "/api/loan-applications/status/*");
    Menu loanAppList =
        findOrCreateMenu("LOAN_APP_LIST", "List All Loan Applications", "/api/loan-applications");
    Menu loanAppUpdate =
        findOrCreateMenu("LOAN_APP_UPDATE", "Update Loan Application", "/api/loan-applications/*");
    Menu loanAppDelete =
        findOrCreateMenu("LOAN_APP_DELETE", "Delete Loan Application", "/api/loan-applications/*");

    // -------------------- LOAN HISTORY (ADMIN/STAFF) --------------------
    Menu loanHistoryMy =
        findOrCreateMenu("LOAN_HISTORY_MY", "My Loan History", "/api/loan-applications/my-history");
    Menu loanHistoryCreate =
        findOrCreateMenu("LOAN_HISTORY_CREATE", "Create Loan History", "/api/loan-history");
    Menu loanHistoryGet =
        findOrCreateMenu("LOAN_HISTORY_GET", "Get Loan History", "/api/loan-history/*");
    Menu loanHistoryByLoan =
        findOrCreateMenu("LOAN_HISTORY_BY_LOAN", "Get History by Loan", "/api/loan-history/loan/*");
    Menu loanHistoryList =
        findOrCreateMenu("LOAN_HISTORY_LIST", "List All Loan Histories", "/api/loan-history");
    Menu loanHistoryDelete =
        findOrCreateMenu("LOAN_HISTORY_DELETE", "Delete Loan History", "/api/loan-history/*");

    // -------------------- PRODUCT MANAGEMENT --------------------
    Menu productCreate = findOrCreateMenu("PRODUCT_CREATE", "Create Product", "/api/products");
    Menu productList = findOrCreateMenu("PRODUCT_LIST", "List All Products", "/api/products");
    Menu productActive =
        findOrCreateMenu("PRODUCT_ACTIVE", "List Active Products", "/api/products/active");
    Menu productByCode =
        findOrCreateMenu("PRODUCT_BY_CODE", "Get Product by Code", "/api/products/code/*");
    Menu productUpdateStatus =
        findOrCreateMenu(
            "PRODUCT_UPDATE_STATUS", "Update Product Status", "/api/products/*/status");
    Menu productDelete = findOrCreateMenu("PRODUCT_DELETE", "Delete Product", "/api/products/*");

    // -------------------- USER PRODUCT (USER) --------------------
    Menu userProductCreate =
        findOrCreateMenu("USER_PRODUCT_CREATE", "Create User Product", "/api/user-products");
    Menu userProductGet =
        findOrCreateMenu("USER_PRODUCT_GET", "Get User Product", "/api/user-products/*");
    Menu userProductByUser =
        findOrCreateMenu(
            "USER_PRODUCT_BY_USER", "Get User Products by User", "/api/user-products/user/*");
    Menu userProductActiveByUser =
        findOrCreateMenu(
            "USER_PRODUCT_ACTIVE", "Get Active User Products", "/api/user-products/user/*/active");
    Menu userProductList =
        findOrCreateMenu("USER_PRODUCT_LIST", "List All User Products", "/api/user-products");
    Menu userProductUpdate =
        findOrCreateMenu("USER_PRODUCT_UPDATE", "Update User Product", "/api/user-products/*");
    Menu userProductDelete =
        findOrCreateMenu("USER_PRODUCT_DELETE", "Delete User Product", "/api/user-products/*");
    Menu userProductMyTier =
        findOrCreateMenu(
            "USER_PRODUCT_MY_TIER", "Get My Tier & Limits", "/api/user-products/my-tier");

    // -------------------- USER PROFILE --------------------
    Menu profileCreate =
        findOrCreateMenu("PROFILE_CREATE", "Create/Update Profile", "/api/user-profiles");
    Menu profileMe = findOrCreateMenu("PROFILE_ME", "Get My Profile", "/api/user-profiles/me");
    Menu profileList = findOrCreateMenu("PROFILE_LIST", "List All Profiles", "/api/user-profiles");
    Menu profileUpdate =
        findOrCreateMenu("PROFILE_UPDATE", "Update My Profile", "/api/user-profiles");
    Menu profileDelete =
        findOrCreateMenu("PROFILE_DELETE", "Delete My Profile", "/api/user-profiles");

    // -------------------- NOTIFICATION --------------------
    Menu notificationCreate =
        findOrCreateMenu("NOTIFICATION_CREATE", "Create Notification", "/api/notifications");
    Menu notificationGet =
        findOrCreateMenu("NOTIFICATION_GET", "Get Notification", "/api/notifications/*");
    Menu notificationByUser =
        findOrCreateMenu(
            "NOTIFICATION_BY_USER", "Get Notifications by User", "/api/notifications/user/*");
    Menu notificationUnreadByUser =
        findOrCreateMenu(
            "NOTIFICATION_UNREAD", "Get Unread Notifications", "/api/notifications/user/*/unread");
    Menu notificationUnreadCount =
        findOrCreateMenu(
            "NOTIFICATION_UNREAD_COUNT",
            "Get Unread Count",
            "/api/notifications/user/*/unread/count");
    Menu notificationList =
        findOrCreateMenu("NOTIFICATION_LIST", "List All Notifications", "/api/notifications");
    Menu notificationMarkRead =
        findOrCreateMenu(
            "NOTIFICATION_MARK_READ", "Mark Notification as Read", "/api/notifications/*/read");
    Menu notificationDelete =
        findOrCreateMenu("NOTIFICATION_DELETE", "Delete Notification", "/api/notifications/*");

    // -------------------- RBAC TEST ENDPOINTS --------------------
    Menu rbacTestMarketing =
        findOrCreateMenu("RBAC_TEST_MARKETING", "RBAC Test Marketing", "/api/test-rbac/marketing");
    Menu rbacTestBranchManager =
        findOrCreateMenu(
            "RBAC_TEST_BRANCH_MANAGER",
            "RBAC Test Branch Manager",
            "/api/test-rbac/branch-manager");
    Menu rbacTestBackOffice =
        findOrCreateMenu(
            "RBAC_TEST_BACK_OFFICE", "RBAC Test Back Office", "/api/test-rbac/back-office");
    Menu rbacTestAdmin =
        findOrCreateMenu("RBAC_TEST_ADMIN", "RBAC Test Admin", "/api/test-rbac/admin-only");

    // -------------------- RBAC MANAGEMENT API --------------------
    Menu rbacRolesList =
        findOrCreateMenu("RBAC_ROLES_LIST", "List Roles Summary", "/api/rbac/roles");
    Menu rbacRoleAccess =
        findOrCreateMenu("RBAC_ROLE_ACCESS", "Get/Update Role Access", "/api/rbac/roles/*/access");
    Menu rbacCategories =
        findOrCreateMenu("RBAC_CATEGORIES", "Get Menu Categories", "/api/rbac/categories");

    // -------------------- UNIFIED STAFF DASHBOARD --------------------
    Menu staffDashboard =
        findOrCreateMenu("STAFF_DASHBOARD", "Staff Dashboard", "/api/staff/dashboard");
    Menu staffQueue = findOrCreateMenu("STAFF_QUEUE", "Staff Queue", "/api/staff/queue");

    // -------------------- FCM (PUSH NOTIFICATION) --------------------
    Menu fcmRegister = findOrCreateMenu("FCM_REGISTER", "Register FCM Token", "/api/fcm/register");
    Menu fcmUnregister =
        findOrCreateMenu("FCM_UNREGISTER", "Unregister FCM Token", "/api/fcm/unregister");
    Menu fcmSend = findOrCreateMenu("FCM_SEND", "Send Push Notification", "/api/fcm/send");
    Menu fcmTest = findOrCreateMenu("FCM_TEST", "Test Push Notification", "/api/fcm/test");

    // ============================================================
    // ROLE-MENU MAPPINGS
    // ============================================================

    // -------------------- USER (CUSTOMER) ROLE --------------------
    mapRoleToMenu(userRole, loanHistoryMy);
    mapRoleToMenu(userRole, loanSubmit);
    mapRoleToMenu(userRole, productList);
    mapRoleToMenu(userRole, productActive);
    mapRoleToMenu(userRole, productByCode);
    mapRoleToMenu(userRole, profileCreate);
    mapRoleToMenu(userRole, profileMe);
    mapRoleToMenu(userRole, profileUpdate);
    mapRoleToMenu(userRole, profileDelete);
    mapRoleToMenu(userRole, notificationByUser);
    mapRoleToMenu(userRole, notificationUnreadByUser);
    mapRoleToMenu(userRole, notificationUnreadCount);
    mapRoleToMenu(userRole, notificationMarkRead);
    mapRoleToMenu(userRole, userProductByUser);
    mapRoleToMenu(userRole, userProductActiveByUser);
    mapRoleToMenu(userRole, userProductMyTier);

    // -------------------- MARKETING ROLE --------------------
    mapRoleToMenu(marketingRole, loanQueueMarketing);
    mapRoleToMenu(marketingRole, loanAction);
    mapRoleToMenu(marketingRole, loanAllowedActions);
    mapRoleToMenu(marketingRole, loanAppGet);
    mapRoleToMenu(marketingRole, loanAppByStatus);
    mapRoleToMenu(marketingRole, loanHistoryGet);
    mapRoleToMenu(marketingRole, loanHistoryByLoan);
    mapRoleToMenu(marketingRole, rbacTestMarketing);
    mapRoleToMenu(marketingRole, productList);
    mapRoleToMenu(marketingRole, productActive);

    // -------------------- BRANCH MANAGER ROLE --------------------
    mapRoleToMenu(branchManagerRole, loanQueueBranchManager);
    mapRoleToMenu(branchManagerRole, loanAction);
    mapRoleToMenu(branchManagerRole, loanAllowedActions);
    mapRoleToMenu(branchManagerRole, loanAppGet);
    mapRoleToMenu(branchManagerRole, loanAppByStatus);
    mapRoleToMenu(branchManagerRole, loanHistoryGet);
    mapRoleToMenu(branchManagerRole, loanHistoryByLoan);
    mapRoleToMenu(branchManagerRole, rbacTestBranchManager);
    mapRoleToMenu(branchManagerRole, productList);
    mapRoleToMenu(branchManagerRole, productActive);

    // -------------------- BACK OFFICE ROLE --------------------
    mapRoleToMenu(backOfficeRole, loanQueueBackOffice);
    mapRoleToMenu(backOfficeRole, loanAction);
    mapRoleToMenu(backOfficeRole, loanAllowedActions);
    mapRoleToMenu(backOfficeRole, loanAppGet);
    mapRoleToMenu(backOfficeRole, loanAppByStatus);
    mapRoleToMenu(backOfficeRole, loanHistoryGet);
    mapRoleToMenu(backOfficeRole, loanHistoryByLoan);
    mapRoleToMenu(backOfficeRole, rbacTestBackOffice);
    mapRoleToMenu(backOfficeRole, productList);
    mapRoleToMenu(backOfficeRole, productActive);

    // -------------------- ADMIN ROLE (FULL ACCESS) --------------------
    // Admin Dashboard
    mapRoleToMenu(adminRole, adminDashboard);
    mapRoleToMenu(adminRole, adminSystemLogs);

    // User Management
    mapRoleToMenu(adminRole, userList);
    mapRoleToMenu(adminRole, userGet);
    mapRoleToMenu(adminRole, userCreate);
    mapRoleToMenu(adminRole, userUpdate);
    mapRoleToMenu(adminRole, userDelete);
    mapRoleToMenu(adminRole, adminUserList);
    mapRoleToMenu(adminRole, adminUserCreate);
    mapRoleToMenu(adminRole, adminUserStatus);
    mapRoleToMenu(adminRole, adminUserRoles);

    // Role Management
    mapRoleToMenu(adminRole, roleList);
    mapRoleToMenu(adminRole, roleCreate);
    mapRoleToMenu(adminRole, roleDelete);

    // Menu Management
    mapRoleToMenu(adminRole, menuList);
    mapRoleToMenu(adminRole, menuGet);
    mapRoleToMenu(adminRole, menuCreate);
    mapRoleToMenu(adminRole, menuUpdate);
    mapRoleToMenu(adminRole, menuDelete);

    // Role-Menu Management
    mapRoleToMenu(adminRole, roleMenuAssign);
    mapRoleToMenu(adminRole, roleMenuGetByRole);
    mapRoleToMenu(adminRole, roleMenuGetByMenu);
    mapRoleToMenu(adminRole, roleMenuRemove);

    // RBAC Management API
    mapRoleToMenu(adminRole, rbacRolesList);
    mapRoleToMenu(adminRole, rbacRoleAccess);
    mapRoleToMenu(adminRole, rbacCategories);

    // FCM (Push Notification) - Admin has full access
    mapRoleToMenu(adminRole, fcmRegister);
    mapRoleToMenu(adminRole, fcmUnregister);
    mapRoleToMenu(adminRole, fcmSend);
    mapRoleToMenu(adminRole, fcmTest);

    // Loan Application Management
    mapRoleToMenu(adminRole, loanAppCreate);
    mapRoleToMenu(adminRole, loanAppGet);
    mapRoleToMenu(adminRole, loanAppByUser);
    mapRoleToMenu(adminRole, loanAppByStatus);
    mapRoleToMenu(adminRole, loanAppList);
    mapRoleToMenu(adminRole, loanAppUpdate);
    mapRoleToMenu(adminRole, loanAppDelete);

    // Loan History Management
    mapRoleToMenu(adminRole, loanHistoryCreate);
    mapRoleToMenu(adminRole, loanHistoryGet);
    mapRoleToMenu(adminRole, loanHistoryByLoan);
    mapRoleToMenu(adminRole, loanHistoryList);
    mapRoleToMenu(adminRole, loanHistoryDelete);

    // Product Management
    mapRoleToMenu(adminRole, productCreate);
    mapRoleToMenu(adminRole, productList);
    mapRoleToMenu(adminRole, productActive);
    mapRoleToMenu(adminRole, productByCode);
    mapRoleToMenu(adminRole, productUpdateStatus);
    mapRoleToMenu(adminRole, productDelete);

    // User Product Management
    mapRoleToMenu(adminRole, userProductCreate);
    mapRoleToMenu(adminRole, userProductGet);
    mapRoleToMenu(adminRole, userProductByUser);
    mapRoleToMenu(adminRole, userProductActiveByUser);
    mapRoleToMenu(adminRole, userProductList);
    mapRoleToMenu(adminRole, userProductUpdate);
    mapRoleToMenu(adminRole, userProductDelete);

    // User Profile Management
    mapRoleToMenu(adminRole, profileCreate);
    mapRoleToMenu(adminRole, profileMe);
    mapRoleToMenu(adminRole, profileList);
    mapRoleToMenu(adminRole, profileUpdate);
    mapRoleToMenu(adminRole, profileDelete);

    // Notification Management
    mapRoleToMenu(adminRole, notificationCreate);
    mapRoleToMenu(adminRole, notificationGet);
    mapRoleToMenu(adminRole, notificationByUser);
    mapRoleToMenu(adminRole, notificationUnreadByUser);
    mapRoleToMenu(adminRole, notificationUnreadCount);
    mapRoleToMenu(adminRole, notificationList);
    mapRoleToMenu(adminRole, notificationMarkRead);
    mapRoleToMenu(adminRole, notificationDelete);

    // Loan Workflow (Admin has full access)
    mapRoleToMenu(adminRole, loanSubmit);
    mapRoleToMenu(adminRole, loanAction);
    mapRoleToMenu(adminRole, loanAllowedActions);
    mapRoleToMenu(adminRole, loanQueueMarketing);
    mapRoleToMenu(adminRole, loanQueueBranchManager);
    mapRoleToMenu(adminRole, loanQueueBackOffice);

    // RBAC Test
    mapRoleToMenu(adminRole, rbacTestAdmin);
    mapRoleToMenu(adminRole, rbacTestMarketing);
    mapRoleToMenu(adminRole, rbacTestBranchManager);
    mapRoleToMenu(adminRole, rbacTestBackOffice);

    // Unified Staff Dashboard (accessible by all workflow roles)
    mapRoleToMenu(adminRole, staffDashboard);
    mapRoleToMenu(adminRole, staffQueue);
    mapRoleToMenu(marketingRole, staffDashboard);
    mapRoleToMenu(marketingRole, staffQueue);
    mapRoleToMenu(branchManagerRole, staffDashboard);
    mapRoleToMenu(branchManagerRole, staffQueue);
    mapRoleToMenu(backOfficeRole, staffDashboard);
    mapRoleToMenu(backOfficeRole, staffQueue);

    // Permission-based menus for workflow actions
    // Marketing: Can review loans (SUBMITTED, IN_REVIEW statuses)
    mapRoleToMenu(marketingRole, loanReview);
    mapRoleToMenu(marketingRole, loanQueueMarketing);
    mapRoleToMenu(marketingRole, loanAction);
    mapRoleToMenu(marketingRole, loanAllowedActions);
    mapRoleToMenu(marketingRole, loanAppGet);
    mapRoleToMenu(marketingRole, loanHistoryByLoan);
    mapRoleToMenu(marketingRole, productList);
    mapRoleToMenu(marketingRole, productActive);

    // Branch Manager: Can approve/reject loans (WAITING_APPROVAL status)
    mapRoleToMenu(branchManagerRole, loanApprove);
    mapRoleToMenu(branchManagerRole, loanReject);
    mapRoleToMenu(branchManagerRole, loanQueueBranchManager);
    mapRoleToMenu(branchManagerRole, loanAction);
    mapRoleToMenu(branchManagerRole, loanAllowedActions);
    mapRoleToMenu(branchManagerRole, loanAppGet);
    mapRoleToMenu(branchManagerRole, loanHistoryByLoan);
    mapRoleToMenu(branchManagerRole, productList);
    mapRoleToMenu(branchManagerRole, productActive);

    // Back Office: Can disburse loans (APPROVED_WAITING_DISBURSEMENT status) - sees
    // all branches
    mapRoleToMenu(backOfficeRole, loanDisburse);
    mapRoleToMenu(backOfficeRole, loanQueueBackOffice);
    mapRoleToMenu(backOfficeRole, loanAction);
    mapRoleToMenu(backOfficeRole, loanAllowedActions);
    mapRoleToMenu(backOfficeRole, loanAppGet);
    mapRoleToMenu(backOfficeRole, loanAppByStatus);
    mapRoleToMenu(backOfficeRole, loanHistoryByLoan);
    mapRoleToMenu(backOfficeRole, productList);
    mapRoleToMenu(backOfficeRole, productActive);

    // ============================================================
    // BRANCHES INITIALIZATION
    // ============================================================
    Branch jakartaBranch = findOrCreateBranch("JKT", "Jakarta", "Jl. Sudirman No. 1, Jakarta");
    Branch surabayaBranch =
        findOrCreateBranch("SBY", "Surabaya", "Jl. Basuki Rahmat No. 10, Surabaya");
    Branch semarangBranch = findOrCreateBranch("SMG", "Semarang", "Jl. Pemuda No. 5, Semarang");

    // ============================================================
    // TEST USERS (with branch assignments for staff)
    // ============================================================
    createTestUser("admin", "admin@example.com", "admin123", null, adminRole, userRole);
    createTestUser("marketing", "marketing@example.com", "pass123", jakartaBranch, marketingRole);
    createTestUser("manager", "manager@example.com", "pass123", jakartaBranch, branchManagerRole);
    createTestUser("backoffice", "backoffice@example.com", "pass123", null, backOfficeRole);
    createTestUser("user", "user@example.com", "pass123", null, userRole);
    // Additional users for other branches
    createTestUser(
        "marketing_sby", "marketing_sby@example.com", "pass123", surabayaBranch, marketingRole);
    createTestUser(
        "manager_sby", "manager_sby@example.com", "pass123", surabayaBranch, branchManagerRole);
    createTestUser(
        "marketing_smg", "marketing_smg@example.com", "pass123", semarangBranch, marketingRole);
    createTestUser(
        "manager_smg", "manager_smg@example.com", "pass123", semarangBranch, branchManagerRole);

    // ============================================================
    // TIER PRODUCTS
    // ============================================================
    initializeTierProducts();

    System.out.println("✓ Data initialization completed!");
    System.out.println("✓ Roles created: ADMIN, USER, BACK_OFFICE, BRANCH_MANAGER, MARKETING");
    System.out.println("✓ Branches created: Jakarta, Surabaya, Semarang");
    System.out.println("✓ Menus & RBAC seeded with all endpoint mappings");
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

  private void createTestUser(
      String username, String email, String password, Branch branch, Role... roles) {
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
                      .branch(branch)
                      .build();

              return userRepository.save(user);
            });
  }

  private Branch findOrCreateBranch(String code, String name, String address) {
    return branchRepository
        .findByCode(code)
        .orElseGet(
            () -> {
              Branch branch =
                  Branch.builder().code(code).name(name).address(address).isActive(true).build();
              System.out.println("✓ Created branch: " + name);
              return branchRepository.save(branch);
            });
  }

  private void cleanupDeprecatedMenus() {
    List<String> deprecatedCodes =
        List.of(
            "MARKETING_DASHBOARD",
            "MARKETING_STATS",
            "BRANCH_MANAGER_DASHBOARD",
            "BRANCH_MANAGER_REPORTS",
            "BACK_OFFICE_DASHBOARD",
            "BACK_OFFICE_DISBURSEMENTS");

    for (String code : deprecatedCodes) {
      menuRepository
          .findByCode(code)
          .ifPresent(
              menu -> {
                // Delete role mappings
                List<RoleMenu> mappings = roleMenuRepository.findByMenuId(menu.getMenuId());
                if (!mappings.isEmpty()) {
                  roleMenuRepository.deleteAll(mappings);
                  System.out.println(
                      "Removed " + mappings.size() + " role mappings for deprecated menu: " + code);
                }
                // Delete menu
                menuRepository.delete(menu);
                System.out.println("Removed deprecated menu: " + code);
              });
    }
  }
}
