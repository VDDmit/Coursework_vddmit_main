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
@Table(name = "team_achievement")
public class TeamAchievement {
    @Id
    @UuidGenerator
    private String id;

    @NotNull
    @Column(nullable = false)
    private Integer points;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(length = 255)
    private String description;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;
}
