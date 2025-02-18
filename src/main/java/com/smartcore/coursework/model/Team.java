package com.smartcore.coursework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
@Table(name = "team")
public class Team {
    @Id
    @UuidGenerator
    private String id;

    @Column(length = 255)
    private String name;

    @OneToOne
    @JoinColumn(name = "leader_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private AppUser leader;

    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                // избегаем рекурсии с leader
                ", leader=" + (leader != null ? leader.getUsername() : "null") +
                '}';
    }
}
