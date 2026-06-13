package io.evcharge.user.service;

import io.evcharge.common.jwt.JwtUtil;
import io.evcharge.user.api.dto.*;
import io.evcharge.user.converter.UserAccountToUserResponseDtoConverter;
import io.evcharge.user.domain.Role;
import io.evcharge.user.domain.UserAccount;
import io.evcharge.user.exception.ApiException;
import io.evcharge.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserAccountToUserResponseDtoConverter converter;

    @Value("${security.jwt.expiration-ms}") long expiration;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        UserAccount u = UserAccount.builder()
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();
        userRepository.save(u);
        log.info("Registered new user id={} email={}", u.getId(), u.getEmail());
        return tokenFor(u);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        UserAccount u = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.LOCKED,
                    "Account locked until " + u.getLockedUntil());
        }
        if (!u.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account disabled");
        }
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            u.setFailedLoginAttempts(u.getFailedLoginAttempts() + 1);
            if (u.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                u.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                log.warn("User {} locked due to repeated failed login", u.getEmail());
            }
            userRepository.save(u);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        u.setFailedLoginAttempts(0);
        u.setLockedUntil(null);
        userRepository.save(u);
        return tokenFor(u);
    }

    private AuthResponse tokenFor(UserAccount u) {
        List<String> roles = u.getRoles().stream().map(Enum::name).toList();
        String token = jwtUtil.generate(u.getId(), u.getEmail(), roles);
        return AuthResponse.of(token, expiration, converter.convert(u));
    }
}
