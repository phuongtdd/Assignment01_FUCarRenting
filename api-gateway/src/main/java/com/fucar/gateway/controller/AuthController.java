package com.fucar.gateway.controller;

import com.fucar.gateway.dto.CustomerAuthResponse;
import com.fucar.gateway.dto.LoginRequest;
import com.fucar.gateway.dto.LoginResponse;
import com.fucar.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.service.customer}")
    private String customerServiceUrl;

    /**
     * Unified login endpoint for both Admin and Customer.
     *
     * - If credentials match the admin account (from application.properties), returns ADMIN token immediately.
     * - Otherwise delegates to customer-service internal authenticate endpoint.
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        // 1. Check admin credentials
        if (adminEmail.equals(request.getEmail()) && adminPassword.equals(request.getPassword())) {
            String token = jwtUtil.generateToken(0L, "ADMIN", adminEmail);
            LoginResponse response = new LoginResponse(token, "ADMIN", 0L, adminEmail);
            log.info("Admin logged in: {}", adminEmail);
            return Mono.just(ResponseEntity.ok(response));
        }

        // 2. Delegate to customer-service for customer login
        return webClientBuilder.build()
                .post()
                .uri(customerServiceUrl + "/internal/customers/authenticate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CustomerAuthResponse.class)
                .map(customerAuth -> {
                    String token = jwtUtil.generateToken(customerAuth.getCustomerId(), "CUSTOMER", customerAuth.getEmail());
                    LoginResponse response = new LoginResponse(token, "CUSTOMER", customerAuth.getCustomerId(), customerAuth.getEmail());
                    log.info("Customer logged in: {}", customerAuth.getEmail());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(WebClientResponseException.Unauthorized.class, e -> {
                    log.warn("Failed login attempt for email: {}", request.getEmail());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Customer service unavailable during login: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }
}
