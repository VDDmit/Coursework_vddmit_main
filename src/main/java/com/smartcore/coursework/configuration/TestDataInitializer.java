package com.smartcore.coursework.configuration;

import com.smartcore.coursework.model.*;
import com.smartcore.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

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
    private final UserProjectRepository userProjectRepository;

    private final Set<String> testUsernames = Set.of("admin", "moderator", "user1", "user2", "user3", "user4", "user5", "user6");

    AppUser initializeUsers() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
        Role moderatorRole = roleRepository.findByName("MODERATOR").orElseThrow(() -> new RuntimeException("Role MODERATOR not found"));
        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Role USER not found"));

        AppUser adminUser = createUserIfNotExists("admin", "admin@example.com", "adminPassword", adminRole, 9, 8999);
        createUserIfNotExists("moderator", "moderator@example.com", "moderatorPassword", moderatorRole, 5, 4111);
        createUserIfNotExists("user1", "user1@example.com", "user1Password", userRole, 2, 1004);
        createUserIfNotExists("user2", "user2@example.com", "user2Password", userRole, 3, 3004);
        createUserIfNotExists("user3", "user3@example.com", "user3Password", userRole, 1, 300);
        createUserIfNotExists("user4", "user4@example.com", "user4Password", userRole, 3, 3453);
        createUserIfNotExists("user5", "user5@example.com", "user5Password", userRole, 3, 3555);
        createUserIfNotExists("user6", "user6@example.com", "user6Password", userRole, 6, 5555);

        return adminUser;
    }

    private AppUser createUserIfNotExists(String username, String email, String password, Role role, int lvl, int xp) {
        return appUserRepository.findByUsername(username).orElseGet(() -> {
            AppUser user = AppUser.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .lvl(lvl)
                    .xp(xp)
                    .build();
            appUserRepository.save(user);
            log.info("Created user: {}", username);
            return user;
        });
    }

    void initializeProjectsAndTasks(AppUser adminUser) {
        if (adminUser == null) {
            log.error("Admin user is null. Skipping project and task creation.");
            return;
        }

        // Создание проектов
        List<Project> projects = List.of(
                createProjectIfNotExists("Project 1", "This is the first project", adminUser),
                createProjectIfNotExists("Project 2", "This is the second project", adminUser),
                createProjectIfNotExists("Project 3", "This is the third project", adminUser),
                createProjectIfNotExists("Project 4", "This is the fourth project", adminUser)
        );

        // Создание задач для админа
        createTaskIfNotExists("Task 1 for Project 1", "Sorting algorithms...", false, adminUser, projects.get(0), 50);
        createTaskIfNotExists("Task 2 for Project 1", "Second task in Project 1", true, adminUser, projects.get(0), 100);
        createTaskIfNotExists("Task 1 for Project 2", "First task in Project 2", false, adminUser, projects.get(1), 30);
        createTaskIfNotExists("Task 2 for Project 2", "Second task in Project 2", false, adminUser, projects.get(1), 70);
        createTaskIfNotExists("Task 1 for Project 3", "First task in Project 3", false, adminUser, projects.get(2), 80);
        createTaskIfNotExists("Task 2 for Project 3", "Second task in Project 3", true, adminUser, projects.get(2), 120);
        createTaskIfNotExists("Task 1 for Project 4", "First task in Project 4", false, adminUser, projects.get(3), 60);
        createTaskIfNotExists("Task 2 for Project 4", "Second task in Project 4", false, adminUser, projects.get(3), 90);

        // Добавляем пользователей на проекты
        addUsersToProjects(projects);
    }

    private void addUsersToProjects(List<Project> projects) {
        List<AppUser> testUsers = appUserRepository.findAll().stream()
                .filter(user -> testUsernames.contains(user.getUsername())
                        && user.getRole().getName().equals("USER")
                        || user.getRole().getName().equals("MODERATOR"))
                .toList();

        // Разбиваем пользователей по проектам
        for (int i = 0; i < testUsers.size(); i++) {
            AppUser user = testUsers.get(i);
            Project project = projects.get(i % projects.size()); // Назначаем проект по кругу
            if (!userProjectRepository.existsByUserAndProject(user, project)) {
                UserProject userProject = UserProject.builder()
                        .user(user)
                        .project(project)
                        .build();
                userProjectRepository.save(userProject);
                log.info("Assigned user {} to project {}", user.getUsername(), project.getName());
            }
        }
    }


    private Project createProjectIfNotExists(String name, String description, AppUser owner) {
        return projectRepository.findByName(name).orElseGet(() -> {
            Project project = Project.builder()
                    .name(name)
                    .description(description)
                    .owner(owner)
                    .build();

            projectRepository.save(project);
            log.info("Created project: {}", name);
            return project;
        });
    }

    private void createTaskIfNotExists(String title, String description, boolean completed, AppUser user, Project project, int xp) {
        if (!taskRepository.existsByTitleAndProject(title, project)) {
            Task task = Task.builder()
                    .title(title)
                    .description(description)
                    .completed(completed)
                    .assignedUser(user)
                    .project(project)
                    .xp(xp)
                    .build();
            taskRepository.save(task);
            log.info("Created task: {}", title);
        }
    }

    void initializeTeams() {
        List<AppUser> leaders = appUserRepository.findAll().stream()
                .filter(user -> testUsernames.contains(user.getUsername()) &&
                        (user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("MODERATOR")))
                .toList();

        List<AppUser> testUsers = appUserRepository.findAll().stream()
                .filter(user -> testUsernames.contains(user.getUsername()) && user.getRole().getName().equals("USER"))
                .toList();

        if (leaders.isEmpty()) {
            log.warn("No eligible leaders found. Skipping team initialization.");
            return;
        }

        for (AppUser leader : leaders) {
            Team team = teamRepository.findByName("Team " + leader.getUsername()).orElseGet(() -> {
                Team newTeam = Team.builder()
                        .name("Team " + leader.getUsername())
                        .leader(leader)
                        .build();
                teamRepository.save(newTeam);
                log.info("Created team: {}", newTeam.getName());
                return newTeam;
            });

            if (leader.getTeam() == null) {
                leader.setTeam(team);
                appUserRepository.save(leader);
                log.info("Assigned {} as leader to {}", leader.getUsername(), team.getName());
            }
        }

        List<Team> teams = teamRepository.findAll();
        for (int i = 0; i < testUsers.size(); i++) {
            AppUser user = testUsers.get(i);
            if (user.getTeam() == null) {
                Team team = teams.get(i % teams.size());
                user.setTeam(team);
                appUserRepository.save(user);
                log.info("Assigned {} to {}", user.getUsername(), team.getName());
            }
        }
    }
}
