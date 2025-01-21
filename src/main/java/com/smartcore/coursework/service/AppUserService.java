package com.smartcore.coursework.service;

import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUser getAppUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found."));
    }

    public AppUser getAppUserById(String id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id " + id + " not found."));
    }

    public void save(AppUser appUser) {
        if (appUser.getPassword() != null && !appUser.getPassword().isEmpty()) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        }
        appUserRepository.save(appUser);
    }
}
