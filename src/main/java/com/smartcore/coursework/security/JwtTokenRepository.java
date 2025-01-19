package com.smartcore.coursework.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.WebUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Repository
public class JwtTokenRepository implements CsrfTokenRepository {

    @Value("${spring.security.jwt.secret}")
    private String secretKey;

    @Value("${spring.security.jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    @Value("${spring.security.jwt.refresh-token-expiration-days}")
    private int refreshTokenExpirationDays;

    private static final String CSRF_COOKIE_NAME = "_csrf";
    private static final String CSRF_HEADER_NAME = "x-csrf-token";

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        try {
            log.info("Starting token generation...");

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(new Date())
                    .expirationTime(new Date(new Date().getTime() + accessTokenExpirationMinutes * 60 * 1000L))
                    .build();

            JWSSigner signer = new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8));
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
            log.info("Token successfully generated: {}", token);
            return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_COOKIE_NAME, token);
        } catch (JOSEException e) {
            log.error("Error generating CSRF token: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating CSRF token", e);
        }
    }

    @Override
    public void saveToken(CsrfToken csrfToken, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, csrfToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(accessTokenExpirationMinutes * 60);
        response.addCookie(cookie);
        log.info("CSRF token saved to cookie.");
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, CSRF_COOKIE_NAME);
        if (cookie != null) {
            String token = cookie.getValue();
            if (validateToken(token)) {
                return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_COOKIE_NAME, token);
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));
            if (!signedJWT.verify(verifier)) {
                log.error("Token signature is invalid.");
                return false;
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && expirationTime.after(new Date());
        } catch (ParseException | JOSEException e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

    public String generateAccessToken() {
        return generateToken(accessTokenExpirationMinutes * 60 * 1000L);
    }

    public String generateRefreshToken() {
        return generateToken(refreshTokenExpirationDays * 24 * 60 * 60 * 1000L);
    }

    private String generateToken(long durationInMillis) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(new Date())
                    .expirationTime(new Date(new Date().getTime() + durationInMillis))
                    .build();

            JWSSigner signer = new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8));
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Error generating token: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating token", e);
        }
    }
}