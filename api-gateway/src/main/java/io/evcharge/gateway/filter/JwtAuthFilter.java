package io.evcharge.gateway.filter;

import io.evcharge.common.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * Global JWT filter — validates Bearer token, injects user id/email/roles
 * as X-User-* headers for downstream services.
 *
 * Public paths (no token required): /api/users/login, /api/users/register, /actuator/**
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC = Set.of(
            "/api/users/login",
            "/api/users/register",
            "/actuator/health"
    );

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (PUBLIC.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        String token = auth.substring(7);
        try {
            Claims claims = jwtUtil.parse(token);
            List<String> roles = JwtUtil.extractRoles(claims);
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Email", String.valueOf(claims.get("email")))
                    .header("X-User-Roles", String.join(",", roles))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange ex, String msg) {
        ex.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        ex.getResponse().getHeaders().add("X-Auth-Error", msg);
        return ex.getResponse().setComplete();
    }

    @Override public int getOrder() { return -100; }
}
