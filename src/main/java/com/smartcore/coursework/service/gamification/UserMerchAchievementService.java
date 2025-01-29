package com.smartcore.coursework.service.gamification;

import com.smartcore.coursework.dto.UserMerchAchievementDTO;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.UserMerchAchievement;
import com.smartcore.coursework.repository.UserMerchAchievementRepository;
import com.smartcore.coursework.service.AppUserAndTokenService;
import com.smartcore.coursework.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMerchAchievementService {
    private final UserMerchAchievementRepository userMerchAchievementRepository;
    private final UserMerchAchievementRepository achievementRepository;
    private final AppUserAndTokenService appUserAndTokenService;

    public List<UserMerchAchievement> getAllUserMerchAchievementsByUserUsername(String username) {
        appUserAndTokenService.getAppUserByUsername(username); // The service already has all existence checks
        return userMerchAchievementRepository.findByUserUsername(username);
    }

    public UserMerchAchievement issueTheAchievement(String username, UserMerchAchievementDTO achievementDTO) {
        AppUser user = appUserAndTokenService.getAppUserByUsername(username);

        boolean achievementExists = userMerchAchievementRepository
                .existsByNameAndDescription(achievementDTO.getName(), achievementDTO.getDescription());
        if (achievementExists) {
            throw new RuntimeException("Achievement with name "
                    + achievementDTO.getName() + " already exists "
                    + ClassUtils.getClassAndMethodName());
        }

        UserMerchAchievement achievement = UserMerchAchievement.builder()
                .name(achievementDTO.getName())
                .description(achievementDTO.getDescription())
                .obtained(true)
                .user(user)
                .build();

        return achievementRepository.save(achievement);
    }
}
