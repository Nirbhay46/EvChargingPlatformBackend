package io.evcharge.user.service;

import io.evcharge.user.domain.Role;
import io.evcharge.user.domain.UserAccount;
import io.evcharge.user.exception.ApiException;
import io.evcharge.user.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @InjectMocks UserService service;

    @Test
    void findById_returnsUserResponse() {
        UserAccount u = UserAccount.builder()
                .id(1L).email("a@b.com").fullName("A").passwordHash("h")
                .roles(Set.of(Role.ROLE_USER)).enabled(true).build();
        when(repo.findById(1L)).thenReturn(Optional.of(u));

        var res = service.findById(1L);

        assertThat(res.email()).isEqualTo("a@b.com");
        assertThat(res.roles()).containsExactly("ROLE_USER");
    }

    @Test
    void findById_throws_whenMissing() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void findByEmail_isCaseInsensitive() {
        UserAccount u = UserAccount.builder().id(2L).email("z@x.io").fullName("Z")
                .passwordHash("h").roles(Set.of(Role.ROLE_USER)).enabled(true).build();
        when(repo.findByEmailIgnoreCase("Z@X.IO")).thenReturn(Optional.of(u));

        assertThat(service.findByEmail("Z@X.IO").id()).isEqualTo(2L);
    }
}
