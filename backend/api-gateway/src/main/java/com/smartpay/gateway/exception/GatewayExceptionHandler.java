package com.smartpay.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Service temporarily unavailable. Please try again later.";

        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Target downstream service is unavailable";
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("timestamp", Instant.now().toString());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", message);
        errorAttributes.put("path", exchange.getRequest().getURI().getPath());
        errorAttributes.put("correlationId", correlationId != null ? correlationId : "N/A");

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(errorAttributes).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = "{\"error\":\"Internal Server Error\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
