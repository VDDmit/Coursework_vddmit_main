package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.AccessLevel;
import com.smartcore.coursework.model.Role;
import com.smartcore.coursework.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("roleInitializer")
@RequiredArgsConstructor
@Slf4j
class RoleInitializer {
    private final RoleRepository roleRepository;

    @Transactional
    void initializeRoles() {
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

            log.info("Roles initialized successfully.");
        }
    }
}
