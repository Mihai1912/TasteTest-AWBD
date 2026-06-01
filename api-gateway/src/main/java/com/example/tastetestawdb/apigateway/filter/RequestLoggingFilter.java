package com.example.tastetestawdb.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class RequestLoggingFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID = "X-Correlation-ID";
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String headerCorrelationId = exchange.getRequest().getHeaders()
                .getFirst(CORRELATION_ID);
        final String correlationId = (headerCorrelationId == null || headerCorrelationId.isEmpty())
                ? UUID.randomUUID().toString()
                : headerCorrelationId;

        // Add correlation ID to MDC for logging
        MDC.put("correlationId", correlationId);

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID, correlationId);

        // Log request details
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        logger.info(">>> Incoming request - Method: {}, Path: {}, ClientIP: {}, CorrelationID: {}",
                method, path, remoteAddress, correlationId);

        return chain.filter(exchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            int status = exchange.getResponse().getStatusCode() != null ?
                    exchange.getResponse().getStatusCode().value() : 0;

            logger.info("<<< Outgoing response - Method: {}, Path: {}, Status: {}, Duration: {}ms, CorrelationID: {}",
                    method, path, status, duration, correlationId);

            // Clean up MDC
            MDC.remove("correlationId");
        });
    }
}
