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
@Table(name = "user_merch_achievement")
public class UserMerchAchievement {
    @Id
    @UuidGenerator
    private String id;

    @NotNull
    @Column(nullable = false)
    private boolean obtained;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(length = 255)
    private String description;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;
}
