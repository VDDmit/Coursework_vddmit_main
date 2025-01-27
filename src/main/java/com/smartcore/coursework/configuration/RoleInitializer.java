package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.AccessLevel;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.Role;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder()
                    .name("ADMIN")
                    .accessLevel(AccessLevel.HIGH)
                    .description("Administrator role with full access")
                    .build());

            roleRepository.save(Role.builder()
                    .name("MODERATOR")
                    .accessLevel(AccessLevel.MEDIUM)
                    .description("Moderator role with limited access")
                    .build());

            roleRepository.save(Role.builder()
                    .name("USER")
                    .accessLevel(AccessLevel.LOW)
                    .description("Basic user role with minimal access")
                    .build());
        }
        if (!appUserRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            AppUser adminUser = AppUser.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(new BCryptPasswordEncoder().encode("adminPassword"))
                    .role(adminRole)
                    .lvl(1)
                    .xp(0)
                    .build();

            appUserRepository.save(adminUser);
            System.out.println("Admin user created successfully.");
        }
    }
}
