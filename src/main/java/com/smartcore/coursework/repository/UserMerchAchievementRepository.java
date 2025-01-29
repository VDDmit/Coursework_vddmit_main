package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.UserMerchAchievement;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMerchAchievementRepository extends JpaRepository<UserMerchAchievement, String> {
    List<UserMerchAchievement> findByUserUsername(String username);

    boolean existsByNameAndDescription(@NotNull String name, String description);
}
