package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainInitializer implements CommandLineRunner {

    private final RoleInitializer roleInitializer;
    private final TestDataInitializer testDataInitializer;

    @Override
    public void run(String... args) {
        roleInitializer.initializeRoles();
        AppUser adminUser = testDataInitializer.initializeUsers();
        testDataInitializer.initializeProjectsAndTasks(adminUser);
        testDataInitializer.initializeTeams();
    }
}
