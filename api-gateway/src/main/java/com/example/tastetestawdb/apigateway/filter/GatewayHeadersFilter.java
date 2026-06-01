package com.example.tastetestawdb.apigateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class GatewayHeadersFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.remove("X-Internal-Token"))
                .header("X-Gateway-Request-Id", UUID.randomUUID().toString())
                .build();

        ServerWebExchange mutated = exchange.mutate().request(request).build();
        ServerHttpResponse response = mutated.getResponse();
        response.beforeCommit(() -> {
            response.getHeaders().add("X-Gateway-Processed", "true");
            return Mono.empty();
        });

        return chain.filter(mutated);
    }
}
