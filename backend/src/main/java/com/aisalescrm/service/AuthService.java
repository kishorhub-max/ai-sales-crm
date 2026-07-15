package com.aisalescrm.service;

import com.aisalescrm.dto.request.LoginRequest;
import com.aisalescrm.dto.request.RegisterRequest;
import com.aisalescrm.dto.response.AuthResponse;
import com.aisalescrm.entity.User;
import com.aisalescrm.exception.BusinessException;
import com.aisalescrm.exception.DuplicateResourceException;
import com.aisalescrm.repository.UserRepository;
import com.aisalescrm.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getEmail(), saved.getRole());

        return buildAuthResponse(saved);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException automatically if wrong credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!user.isActive()) {
            throw new BusinessException("Account is deactivated. Contact your administrator.");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── Refresh Token ────────────────────────────────────────────────────────

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        return buildAuthResponse(user);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}