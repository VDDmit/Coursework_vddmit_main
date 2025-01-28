package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.UserMerchAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMerchAchievementRepository extends JpaRepository<UserMerchAchievement, String> {
}
