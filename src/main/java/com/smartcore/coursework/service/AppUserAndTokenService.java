package com.smartcore.coursework.service;

import com.smartcore.coursework.exception.EntityNotFoundException;
import com.smartcore.coursework.model.AccessLevel;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.RefreshToken;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.RefreshTokenRepository;
import com.smartcore.coursework.security.JwtTokenRepository;
import com.smartcore.coursework.util.ClassUtils;
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
        validateInput(email, "Email");
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found in " + ClassUtils.getClassAndMethodName()));
    }

    public AppUser getAppUserByUsername(String username) {
        validateInput(username, "Username");
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found in " + ClassUtils.getClassAndMethodName()));
    }

    public void save(AppUser appUser) {
        if (appUser == null) {
            throw new IllegalArgumentException("AppUser cannot be null in " + ClassUtils.getClassAndMethodName());
        }
        if (appUser.getPassword() != null && !appUser.getPassword().isEmpty()) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        }
        appUserRepository.save(appUser);
    }

    public boolean hasRequiredAccess(String username, AccessLevel requiredAccessLevel) {
        validateInput(username, "Username");

        AppUser appUser = getAppUserByUsername(username);
        if (appUser == null) {
            throw new EntityNotFoundException("User with username " + username + " not found in " + ClassUtils.getClassAndMethodName());
        }

        AccessLevel appUserAccessLevel = appUser.getRole().getAccessLevel();
        return appUserAccessLevel.ordinal() <= requiredAccessLevel.ordinal();
    }

    public AppUser getAppUserById(String userId) {
        validateInput(userId, "User ID");
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId + " in " + ClassUtils.getClassAndMethodName()));
    }

    public boolean validateAccessToken(String accessToken) {
        validateInput(accessToken, "Access token");
        if (!jwtTokenRepository.validateToken(accessToken)) {
            throw new IllegalArgumentException("Invalid access token in " + ClassUtils.getClassAndMethodName());
        }
        return true;
    }

    public boolean validateRefreshToken(String refreshToken) {
        validateInput(refreshToken, "Refresh token");

        RefreshToken refreshTokenFromDb = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token " + refreshToken + " not found in " + ClassUtils.getClassAndMethodName()));

        if (refreshTokenFromDb.getExpirationDate().before(new Date())) {
            throw new IllegalArgumentException("Refresh token is expired in " + ClassUtils.getClassAndMethodName());
        }

        if (!jwtTokenRepository.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token in " + ClassUtils.getClassAndMethodName());
        }
        return true;
    }

    private void validateInput(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }

    public boolean doesUserExist(String username) {
        return appUserRepository.existsByUsername(username);
    }
}