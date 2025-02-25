package com.smartcore.coursework.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "task")
public class Task {
    @Id
    @UuidGenerator
    private String id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private AppUser assignedUser;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 255)
    private String description;

    @NotNull
    @Column(nullable = false, length = 255)
    private String title;

    @NotNull
    @Column(nullable = false)
    private Integer xp;
}