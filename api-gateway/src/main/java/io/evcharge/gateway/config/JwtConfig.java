package io.evcharge.gateway.config;

import io.evcharge.common.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Bean
    public JwtUtil jwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long exp,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtUtil(secret, exp, issuer);
    }
}
