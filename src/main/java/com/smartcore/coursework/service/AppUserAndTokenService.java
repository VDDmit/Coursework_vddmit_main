package com.smartcore.coursework.service;

import com.smartcore.coursework.model.AccessLevel;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.RefreshToken;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.RefreshTokenRepository;
import com.smartcore.coursework.security.JwtTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserAndTokenService {
    private final AppUserRepository appUserRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public List<AppUser> getAllAppUsers() {
        return appUserRepository.findAll();
    }

    public AppUser getAppUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found."));
    }

    public AppUser getAppUserByUsername(String username) {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User with username " + username + " not found."));
    }

    public void save(AppUser appUser) {
        if (appUser.getPassword() != null && !appUser.getPassword().isEmpty()) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        }
        appUserRepository.save(appUser);
    }

    public boolean hasRequiredAccess(String username, AccessLevel requiredAccessLevel) {
        AppUser appUser = getAppUserByUsername(username);

        if (appUser == null) {
            return false;
        }

        AccessLevel appUserAccessLevel = appUser.getRole().getAccessLevel();

        return appUserAccessLevel.ordinal() <= requiredAccessLevel.ordinal();
    }

    public AppUser getAppUserById(String userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public boolean validateAccessToken(String accessToken) {
        return jwtTokenRepository.validateToken(accessToken);
    }

    public boolean validateRefreshToken(String refreshToken) {
        RefreshToken refreshTokenFromDb = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token " + refreshToken + " not found."));

        if (refreshTokenFromDb.getExpirationDate().before(new Date())) {
            throw new RuntimeException("Refresh token is expired.");
        }

        if (!jwtTokenRepository.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token.");
        }
        return true;
    }
}
