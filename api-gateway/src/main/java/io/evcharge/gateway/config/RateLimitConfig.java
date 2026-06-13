package io.evcharge.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Rate-limit key resolver: prefers the authenticated user id (set by
 * {@code JwtAuthFilter} as the {@code X-User-Id} header), falling back to
 * the client IP for unauthenticated traffic (e.g. /login, /register).
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            InetSocketAddress addr = exchange.getRequest().getRemoteAddress();
            String ip = addr != null ? addr.getAddress().getHostAddress() : "unknown";
            return Mono.just("ip:" + ip);
        };
    }
}
