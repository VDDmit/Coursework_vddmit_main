package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.AccessLevel;
import com.smartcore.coursework.model.Role;
import com.smartcore.coursework.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

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
    }
}
