package com.example.tastetestawdb.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayRateLimitFilter implements GlobalFilter {

    private static final int LIMIT = 20;
    private static final Duration WINDOW = Duration.ofSeconds(10);

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String clientKey = resolveClientKey(exchange);
        WindowCounter counter = counters.computeIfAbsent(clientKey, key -> new WindowCounter(Instant.now()));
        synchronized (counter) {
            if (Instant.now().isAfter(counter.windowStart.plus(WINDOW))) {
                counter.windowStart = Instant.now();
                counter.requests.set(0);
            }
            if (counter.requests.incrementAndGet() > LIMIT) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null) {
            if (exchange.getRequest().getRemoteAddress().getAddress() != null) {
                return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return exchange.getRequest().getRemoteAddress().getHostString();
        }
        return "unknown";
    }

    private static final class WindowCounter {
        private Instant windowStart;
        private final AtomicInteger requests = new AtomicInteger();

        private WindowCounter(Instant windowStart) {
            this.windowStart = windowStart;
        }
    }
}
