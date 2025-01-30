package com.smartcore.coursework.service;

import com.smartcore.coursework.exception.EntityNotFoundException;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.RefreshToken;
import com.smartcore.coursework.model.Role;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.RefreshTokenRepository;
import com.smartcore.coursework.repository.RoleRepository;
import com.smartcore.coursework.security.JwtTokenRepository;
import com.smartcore.coursework.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenRepository jwtTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;

    public void register(String username, String email, String password, String roleName) {
        validateInput(username, "Username");
        validateInput(email, "Email");
        validateInput(password, "Password");
        validateInput(roleName, "Role name");

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User with username " + username + " already exists in " + ClassUtils.getClassAndMethodName());
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists in " + ClassUtils.getClassAndMethodName());
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role " + roleName + " not found in " + ClassUtils.getClassAndMethodName()));

        AppUser appUser = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .lvl(1)
                .xp(0)
                .build();
        appUserRepository.save(appUser);
    }

    public Map<String, String> authenticate(String username, String password) {
        validateInput(username, "Username");
        validateInput(password, "Password");

        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found in " + ClassUtils.getClassAndMethodName()));

        if (!passwordEncoder.matches(password, appUser.getPassword())) {
            throw new IllegalArgumentException("Incorrect password for username " + username + " in " + ClassUtils.getClassAndMethodName());
        }

        String accessToken = jwtTokenRepository.generateAccessToken(appUser.getUsername());
        String refreshToken = jwtTokenRepository.generateRefreshToken(appUser.getUsername());
        saveRefreshToken(appUser, refreshToken);
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    private void saveRefreshToken(AppUser appUser, String refreshToken) {
        if (appUser == null) {
            throw new IllegalArgumentException("AppUser cannot be null in " + ClassUtils.getClassAndMethodName());
        }
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }

        RefreshToken tokenForDatabase = new RefreshToken();
        tokenForDatabase.setAppUser(appUser);
        tokenForDatabase.setToken(refreshToken);
        tokenForDatabase.setExpirationDate(new Date(System.currentTimeMillis()
                + jwtTokenRepository.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L));
        refreshTokenRepository.save(tokenForDatabase);
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token " + refreshToken + " not found in " + ClassUtils.getClassAndMethodName()));
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token expired, please log in again.");
        }
        String newAccessToken = jwtTokenRepository.generateAccessToken(storedToken.getAppUser().getUsername());
        return Map.of("accessToken", newAccessToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    private void validateInput(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }
}