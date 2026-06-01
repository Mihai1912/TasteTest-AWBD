package com.example.tastetestawdb.apigateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Configuration
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatusCode status;
        String errorMessage;

        if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {
            status = rse.getStatusCode();
            errorMessage = rse.getReason();
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            errorMessage = "Invalid request: " + ex.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = "Internal server error";
            logger.error("Unexpected error in API Gateway", ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        String body = buildErrorResponse(status, errorMessage, exchange);
        return exchange.getResponse().writeWith(
                Mono.just(bufferFactory.wrap(body.getBytes()))
        );
    }

    private String buildErrorResponse(HttpStatusCode status, String message, ServerWebExchange exchange) {
        String reason = (status instanceof HttpStatus hs) ? hs.getReasonPhrase() : "";
        return String.format(
            "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
            Instant.now(),
            status.value(),
            reason,
            escapeJson(message),
            exchange.getRequest().getPath().value()
        );
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}
