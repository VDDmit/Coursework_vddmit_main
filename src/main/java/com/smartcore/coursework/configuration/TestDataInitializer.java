package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.*;
import com.smartcore.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
class TestDataInitializer {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository;


    AppUser initializeUsers() {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new RuntimeException("Role MODERATOR not found"));
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        AppUser adminUser = appUserRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            adminUser = AppUser.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("adminPassword"))
                    .role(adminRole)
                    .lvl(1)
                    .xp(999)
                    .build();
            appUserRepository.save(adminUser);
            log.info("Admin user created.");
        }

        if (!appUserRepository.existsByUsername("moderator")) {
            appUserRepository.save(AppUser.builder()
                    .username("moderator")
                    .email("moderator@example.com")
                    .password(passwordEncoder.encode("moderatorPassword"))
                    .role(moderatorRole)
                    .lvl(5)
                    .xp(4111)
                    .build());
            log.info("Moderator user created.");
        }

        if (!appUserRepository.existsByUsername("user1")) {
            appUserRepository.save(AppUser.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("user1Password"))
                    .role(userRole)
                    .lvl(2)
                    .xp(1004)
                    .build());
        }

        if (!appUserRepository.existsByUsername("user2")) {
            appUserRepository.save(AppUser.builder()
                    .username("user2")
                    .email("user2@example.com")
                    .password(passwordEncoder.encode("user2Password"))
                    .role(userRole)
                    .lvl(3)
                    .xp(3004)
                    .build());
        }

        if (!appUserRepository.existsByUsername("user3")) {
            appUserRepository.save(AppUser.builder()
                    .username("user3")
                    .email("user3@example.com")
                    .password(passwordEncoder.encode("user3Password"))
                    .role(userRole)
                    .lvl(1)
                    .xp(300)
                    .build());
        }

        return adminUser;
    }

    void initializeProjectsAndTasks(AppUser adminUser) {
        if (adminUser == null) {
            log.error("Admin user is null. Skipping project and task creation.");
            return;
        }

        // Создание проектов
        List<Project> projects = List.of(
                Project.builder().name("Project 1").description("This is the first project").owner(adminUser).build(),
                Project.builder().name("Project 2").description("This is the second project").owner(adminUser).build(),
                Project.builder().name("Project 3").description("This is the third project").owner(adminUser).build(),
                Project.builder().name("Project 4").description("This is the fourth project").owner(adminUser).build()
        );

        projectRepository.saveAll(projects);
        log.info("Projects created successfully.");

        // Создание задач для админа
        List<Task> tasks = List.of(
                Task.builder().title("Task 1 for Project 1").description("Studying sorting algorithms is important for optimizing application performance and efficiency.").completed(false).assignedUser(adminUser).project(projects.get(0)).xp(50).build(),
                Task.builder().title("Task 2 for Project 1").description("This is the second task in Project 1").completed(true).assignedUser(adminUser).project(projects.get(0)).xp(100).build(),
                Task.builder().title("Task 1 for Project 2").description("This is the first task in Project 2").completed(false).assignedUser(adminUser).project(projects.get(1)).xp(30).build(),
                Task.builder().title("Task 2 for Project 2").description("This is the second task in Project 2").completed(false).assignedUser(adminUser).project(projects.get(1)).xp(70).build(),
                Task.builder().title("Task 1 for Project 3").description("This is the first task in Project 3").completed(false).assignedUser(adminUser).project(projects.get(2)).xp(80).build(),
                Task.builder().title("Task 2 for Project 3").description("This is the second task in Project 3").completed(true).assignedUser(adminUser).project(projects.get(2)).xp(120).build(),
                Task.builder().title("Task 1 for Project 4").description("This is the first task in Project 4").completed(false).assignedUser(adminUser).project(projects.get(3)).xp(60).build(),
                Task.builder().title("Task 2 for Project 4").description("This is the second task in Project 4").completed(false).assignedUser(adminUser).project(projects.get(3)).xp(90).build()
        );

        taskRepository.saveAll(tasks);
        log.info("Tasks for admin created successfully.");

        // Создание задач для других пользователей
        List<AppUser> users = List.of(
                Objects.requireNonNull(appUserRepository.findByUsername("moderator").orElse(null)),
                Objects.requireNonNull(appUserRepository.findByUsername("user1").orElse(null)),
                Objects.requireNonNull(appUserRepository.findByUsername("user2").orElse(null)),
                Objects.requireNonNull(appUserRepository.findByUsername("user3").orElse(null))
        );

        for (AppUser user : users) {
            if (user != null) {
                taskRepository.save(Task.builder()
                        .title("Task 1 for Project 1 - " + user.getUsername())
                        .description("Task for " + user.getUsername() + " in Project 1")
                        .completed(false)
                        .assignedUser(user)
                        .project(projects.get(0))
                        .xp(50)
                        .build());

                taskRepository.save(Task.builder()
                        .title("Task 2 for Project 2 - " + user.getUsername())
                        .description("Task for " + user.getUsername() + " in Project 2")
                        .completed(true)
                        .assignedUser(user)
                        .project(projects.get(1))
                        .xp(70)
                        .build());
            }
        }

        log.info("Tasks for users created successfully.");
    }

    void initializeTeams() {
        if (teamRepository.count() > 0) {
            log.info("Teams are already initialized.");
            return;
        }

        // Получаем всех админов и модераторов для лидеров команд
        List<AppUser> leaders = appUserRepository.findAll()
                .stream()
                .filter(user -> user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("MODERATOR"))
                .toList();

        List<AppUser> users = appUserRepository.findAll()
                .stream()
                .filter(user -> user.getRole().getName().equals("USER"))
                .toList();

        if (leaders.isEmpty()) {
            log.warn("No eligible leaders found (ADMIN or MODERATOR). Skipping team initialization.");
            return;
        }

        // Создаем команды
        List<Team> teams = leaders.stream()
                .map(leader -> Team.builder()
                        .name("Team " + leader.getUsername())
                        .leader(leader)  // Указываем лидера команды
                        .build())
                .toList();

        teamRepository.saveAll(teams);
        log.info("Teams with leaders initialized successfully.");

        // Назначаем лидеров их же командам
        for (Team team : teams) {
            AppUser leader = team.getLeader();
            leader.setTeam(team);
            appUserRepository.save(leader);
            log.info("Leader {} assigned to their own team {}", leader.getUsername(), team.getName());
        }

        // Распределяем пользователей по командам
        for (int i = 0; i < users.size(); i++) {
            Team team = teams.get(i % teams.size()); // равномерное распределение
            AppUser user = users.get(i);
            user.setTeam(team);
            appUserRepository.save(user);
            log.info("User {} assigned to team {}", user.getUsername(), team.getName());
        }

        log.info("All users have been assigned to teams.");
    }

}
