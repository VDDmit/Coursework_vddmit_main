package com.smartcore.coursework.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @UuidGenerator
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "background_color", length = 255)
    private String backgroundColor;

    @Column(name = "theme", length = 255)
    private String theme;
}
