package com.smartcore.coursework.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = "app_user")
public class AppUser {
    @Id
    @UuidGenerator
    private String id;

    @NotNull
    @Column(nullable = false)
    private Integer lvl;

    @NotNull
    @Column(nullable = false)
    private Integer xp;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Email
    @NotNull
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotNull
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull
    @Column(nullable = false, unique = true, length = 255)
    private String username;
}