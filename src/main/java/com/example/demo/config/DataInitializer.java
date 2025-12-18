package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
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

        System.out.println("✓ Data initialization completed!");
        System.out.println("✓ Roles created: ADMIN, USER");
        System.out.println("✓ Sample user created: admin (with ADMIN and USER roles)");
    }
}
