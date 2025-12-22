package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.Product;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;

        @Override
        public void run(String... args) throws Exception {
                // Initialize roles if they don't exist
                Role adminRole = roleRepository.findByName("ADMIN")
                                .orElseGet(() -> {
                                        Role role = Role.builder()
                                                        .name("ADMIN")
                                                        .build();
                                        return roleRepository.save(role);
                                });

                Role userRole = roleRepository.findByName("USER")
                                .orElseGet(() -> {
                                        Role role = Role.builder()
                                                        .name("USER")
                                                        .build();
                                        return roleRepository.save(role);
                                });

                // Initialize a sample user if it doesn't exist
                userRepository.findByUsername("admin")
                                .orElseGet(() -> {
                                        Set<Role> roles = new HashSet<>();
                                        roles.add(adminRole);
                                        roles.add(userRole);

                                        User user = User.builder()
                                                        .username("admin")
                                                        .email("admin@example.com")
                                                        .password("admin123")
                                                        .isActive(true)
                                                        .roles(roles)
                                                        .build();

                                        return userRepository.save(user);
                                });

                // Initialize sample loan products if they don't exist
                productRepository.findByCode("LOAN-PERSONAL-001")
                                .orElseGet(() -> {
                                        Product product = Product.builder()
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

                productRepository.findByCode("LOAN-MORTGAGE-001")
                                .orElseGet(() -> {
                                        Product product = Product.builder()
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

                productRepository.findByCode("LOAN-VEHICLE-001")
                                .orElseGet(() -> {
                                        Product product = Product.builder()
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
                System.out.println("✓ Roles created: ADMIN, USER");
                System.out.println("✓ Sample user created: admin (with ADMIN and USER roles)");
                System.out.println("✓ Sample loan products created: Personal Loan, Home Mortgage, Vehicle Loan");
        }
}
