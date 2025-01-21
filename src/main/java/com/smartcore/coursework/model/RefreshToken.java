package com.smartcore.coursework.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @NotNull
    @Column(nullable = false, unique = true, length = 512)
    private String token;
    @NotNull
    @Column(nullable = false)
    private Date expirationDate;

    public boolean isExpired() {
        return expirationDate.before(new Date());
    }
}
