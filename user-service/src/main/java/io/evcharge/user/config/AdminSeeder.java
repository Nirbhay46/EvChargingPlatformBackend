package io.evcharge.user.config;

import io.evcharge.user.domain.Role;
import io.evcharge.user.domain.UserAccount;
import io.evcharge.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Idempotently seeds an admin account on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:admin@evcharge.io}") String email;
    @Value("${app.admin.password:Admin@12345}") String password;

    @Override
    public void run(String... args) {
        if (repo.existsByEmailIgnoreCase(email)) {
            log.info("Admin user already present: {}", email);
            return;
        }
        UserAccount admin = UserAccount.builder()
                .email(email)
                .passwordHash(encoder.encode(password))
                .fullName("Platform Admin")
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_OPERATOR, Role.ROLE_USER))
                .enabled(true)
                .build();
        repo.save(admin);
        log.info("Seeded admin user: {}", email);
    }
}
