package com.smartcore.coursework.service.gamification;

import com.smartcore.coursework.dto.UserMerchAchievementDTO;
import com.smartcore.coursework.mail.EmailService;
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
    private final EmailService emailService;

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

    private void setUnissuedAchievement(String username, String achievementName) {
        AppUser user = appUserAndTokenService.getAppUserByUsername(username);
        UserMerchAchievement achievement = UserMerchAchievement.builder()
                .name(achievementName)
                .obtained(false)
                .user(user)
                .build();
        userMerchAchievementRepository.save(achievement);
    }

    public void notifyAdminAboutAchievementRequest(String username, String userEmail, String achievementName) {
        String adminEmail = "danvoropaeff@yandex.ru";
        String subject = "User Award Request: " + achievementName;
        String body = "<h1>New Award Request</h1>" +
                "<p>User <b>" + username + "</b> (" + userEmail + ") has requested the following award:</p>" +
                "<h3>" + achievementName + "</h3>" +
                "<p>Please review and approve the request in the admin panel.</p>" +
                "<p>Thanks,<br>Smart Core</p>";
        setUnissuedAchievement(username, achievementName);
        emailService.sendHtmlEmail(adminEmail, subject, body);
        log.info("Administrator notified about award request '{}'", achievementName);
    }

}
