package io.evcharge.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitConfigTest {

    @Test
    void keyResolver_prefersUserId_whenHeaderPresent() {
        KeyResolver resolver = new RateLimitConfig().userKeyResolver();
        var req = MockServerHttpRequest.get("/api/bookings")
                .header("X-User-Id", "42").build();
        var ex = MockServerWebExchange.from(req);

        StepVerifier.create(resolver.resolve(ex))
                .expectNext("user:42")
                .verifyComplete();
    }

    @Test
    void keyResolver_fallsBackToIp_whenAnonymous() {
        KeyResolver resolver = new RateLimitConfig().userKeyResolver();
        var req = MockServerHttpRequest.get("/api/users/login")
                .remoteAddress(new java.net.InetSocketAddress("10.0.0.1", 1234))
                .build();
        var ex = MockServerWebExchange.from(req);

        StepVerifier.create(resolver.resolve(ex))
                .assertNext(k -> assertThat(k).startsWith("ip:"))
                .verifyComplete();
    }
}
