package com.example.tastetestawdb.apigateway.filter;

import com.example.tastetestawdb.apigateway.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class JwtAuthenticationFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (request.getMethod().toString().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);

        if (token == null) {
            logger.warn("Missing authorization token for path: {}", path);
            return onError(exchange, "Missing authorization token", HttpStatus.UNAUTHORIZED);
        }

        if (!tokenProvider.validateToken(token)) {
            logger.warn("Invalid or expired token for path: {}", path);
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        String email = tokenProvider.getEmailFromToken(token);
        List<String> roles = tokenProvider.getRolesFromToken(token);
        String rolesHeader = roles == null ? "" : String.join(",", roles);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Auth-Email", email == null ? "" : email)
                .header("X-Auth-Roles", rolesHeader)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
            "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            error,
            exchange.getRequest().getPath().value()
        );

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.just(bufferFactory.wrap(body.getBytes())));
    }
}
