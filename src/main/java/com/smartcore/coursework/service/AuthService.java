package com.smartcore.coursework.service;

import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.RefreshToken;
import com.smartcore.coursework.model.Role;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.RefreshTokenRepository;
import com.smartcore.coursework.repository.RoleRepository;
import com.smartcore.coursework.security.JwtTokenRepository;
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
        if (appUserRepository.existsByUsername(username) || appUserRepository.existsByEmail(email)) {
            throw new RuntimeException("User with this username or email already exists");
        }
        Role role = roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));
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
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, appUser.getPassword())) {
            throw new RuntimeException("Incorrect password.");
        }
        String accessToken = jwtTokenRepository.generateAccessToken(appUser.getUsername());
        String refreshToken = jwtTokenRepository.generateRefreshToken(appUser.getUsername());
        saveRefreshToken(appUser, refreshToken);
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    private void saveRefreshToken(AppUser appUser, String refreshToken) {
        RefreshToken tokenForDatabase = new RefreshToken();
        tokenForDatabase.setAppUser(appUser);
        tokenForDatabase.setToken(refreshToken);
        tokenForDatabase.setExpirationDate(new Date(System.currentTimeMillis()
                + jwtTokenRepository.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L));
        refreshTokenRepository.save(tokenForDatabase);
    }
}
