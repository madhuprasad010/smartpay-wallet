package com.smartpay.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.auth.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.user.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${services.wallet.url:http://localhost:8083}")
    private String walletServiceUrl;

    @Value("${services.transaction.url:http://localhost:8084}")
    private String transactionServiceUrl;

    @Value("${services.payment.url:http://localhost:8085}")
    private String paymentServiceUrl;

    @Value("${services.beneficiary.url:http://localhost:8086}")
    private String beneficiaryServiceUrl;

    @Value("${services.notification.url:http://localhost:8087}")
    private String notificationServiceUrl;

    @Value("${services.audit.url:http://localhost:8088}")
    private String auditServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri(authServiceUrl))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri(userServiceUrl))
                .route("wallet-service", r -> r.path("/api/v1/wallets/**")
                        .uri(walletServiceUrl))
                .route("transaction-service", r -> r.path("/api/v1/transactions/**")
                        .uri(transactionServiceUrl))
                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .uri(paymentServiceUrl))
                .route("beneficiary-service", r -> r.path("/api/v1/beneficiaries/**")
                        .uri(beneficiaryServiceUrl))
                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .uri(notificationServiceUrl))
                .route("audit-service", r -> r.path("/api/v1/audit/**")
                        .uri(auditServiceUrl))
                .build();
    }
}
