package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.*;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.ProjectRepository;
import com.smartcore.coursework.repository.RoleRepository;
import com.smartcore.coursework.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        // Инициализация ролей
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

        // Создание администратора
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

            // Инициализация проектов для администратора
            Project project1 = Project.builder()
                    .name("Project 1")
                    .description("This is the first project")
                    .owner(adminUser)
                    .build();

            Project project2 = Project.builder()
                    .name("Project 2")
                    .description("This is the second project")
                    .owner(adminUser)
                    .build();

            projectRepository.save(project1);
            projectRepository.save(project2);
            System.out.println("Projects created successfully.");

            // Инициализация задач для проектов
            Task task1 = Task.builder()
                    .title("Task 1 for Project 1")
                    .description("This is the first task in Project 1")
                    .completed(false)
                    .assignedUser(adminUser)
                    .project(project1)
                    .xp(50)
                    .build();

            Task task2 = Task.builder()
                    .title("Task 2 for Project 1")
                    .description("This is the second task in Project 1")
                    .completed(true)
                    .assignedUser(adminUser)
                    .project(project1)
                    .xp(100)
                    .build();

            Task task3 = Task.builder()
                    .title("Task 1 for Project 2")
                    .description("This is the first task in Project 2")
                    .completed(false)
                    .assignedUser(adminUser)
                    .project(project2)
                    .xp(30)
                    .build();

            Task task4 = Task.builder()
                    .title("Task 2 for Project 2")
                    .description("This is the second task in Project 2")
                    .completed(false)
                    .assignedUser(adminUser)
                    .project(project2)
                    .xp(70)
                    .build();

            taskRepository.save(task1);
            taskRepository.save(task2);
            taskRepository.save(task3);
            taskRepository.save(task4);

            System.out.println("Tasks created successfully.");
        }
    }
}
