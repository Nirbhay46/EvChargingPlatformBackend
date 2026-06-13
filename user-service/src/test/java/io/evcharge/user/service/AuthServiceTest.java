package io.evcharge.user.service;

import io.evcharge.common.jwt.JwtUtil;
import io.evcharge.user.api.dto.LoginRequest;
import io.evcharge.user.api.dto.RegisterRequest;
import io.evcharge.user.domain.Role;
import io.evcharge.user.domain.UserAccount;
import io.evcharge.user.exception.ApiException;
import io.evcharge.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock JwtUtil jwt;
    @InjectMocks AuthService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "expiration", 3_600_000L);
    }

    @Test
    void register_persistsUserAndReturnsToken() {
        var req = new RegisterRequest("a@b.com", "Pass@1234", "Alice");
        when(repo.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(encoder.encode("Pass@1234")).thenReturn("HASH");
        when(repo.save(any(UserAccount.class))).thenAnswer(inv -> {
            UserAccount u = inv.getArgument(0); u.setId(1L); return u;
        });
        when(jwt.generate(eq(1L), eq("a@b.com"), anyList())).thenReturn("TOKEN");

        var res = service.register(req);

        assertThat(res.token()).isEqualTo("TOKEN");
        assertThat(res.user().email()).isEqualTo("a@b.com");
        verify(repo).save(any(UserAccount.class));
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(repo.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        assertThatThrownBy(() ->
                service.register(new RegisterRequest("a@b.com", "Pass@1234", "Alice")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_locksAfterFiveFailedAttempts() {
        UserAccount u = UserAccount.builder().id(7L).email("x@y.com")
                .passwordHash("HASH").fullName("X").roles(Set.of(Role.ROLE_USER))
                .enabled(true).failedLoginAttempts(4).build();
        when(repo.findByEmailIgnoreCase("x@y.com")).thenReturn(Optional.of(u));
        when(encoder.matches("wrong", "HASH")).thenReturn(false);

        assertThatThrownBy(() ->
                service.login(new LoginRequest("x@y.com", "wrong")))
                .isInstanceOf(ApiException.class);

        assertThat(u.getLockedUntil()).isNotNull();
        assertThat(u.getFailedLoginAttempts()).isEqualTo(5);
    }

    @Test
    void login_successResetsFailedAttempts() {
        UserAccount u = UserAccount.builder().id(7L).email("x@y.com")
                .passwordHash("HASH").fullName("X").roles(Set.of(Role.ROLE_USER))
                .enabled(true).failedLoginAttempts(3).build();
        when(repo.findByEmailIgnoreCase("x@y.com")).thenReturn(Optional.of(u));
        when(encoder.matches("right", "HASH")).thenReturn(true);
        when(jwt.generate(eq(7L), eq("x@y.com"), anyList())).thenReturn("TOK");

        var res = service.login(new LoginRequest("x@y.com", "right"));

        assertThat(res.token()).isEqualTo("TOK");
        assertThat(u.getFailedLoginAttempts()).isZero();
    }
}
