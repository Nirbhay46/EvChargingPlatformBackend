package io.evcharge.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.evcharge.user.api.dto.LoginRequest;
import io.evcharge.user.api.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("userdb").withUsername("ev").withPassword("ev");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("security.jwt.secret", () -> "test-secret-test-secret-test-secret-1234567890");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void register_then_login_returns_jwt() throws Exception {
        var reg = new RegisterRequest("itest@example.com", "Pass@1234", "I Test");
        mvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("itest@example.com"));

        var login = new LoginRequest("itest@example.com", "Pass@1234");
        mvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void register_validation_fails_on_short_password() throws Exception {
        var reg = new RegisterRequest("v@v.com", "short", "V");
        mvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }
}
