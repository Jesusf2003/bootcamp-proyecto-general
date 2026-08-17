package com.banco.auth.controller;

import com.banco.auth.dto.LoginRequest;
import com.banco.auth.dto.LoginResponse;
import com.banco.auth.model.User;
import com.banco.auth.service.JwtService;
import com.banco.auth.service.UserStore;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserStore userStore;
    private final JwtService jwtService;

    public AuthController(UserStore userStore, JwtService jwtService) {
        this.userStore = userStore;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userStore.findByUsername(request.getUsername());
        if (user == null || !userStore.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Intento de login fallido para usuario={}", request.getUsername());
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        }
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getCustomerId());
        log.info("Login exitoso usuario={} rol={}", user.getUsername(), user.getRole());
        return Mono.just(LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .role(user.getRole())
                .customerId(user.getCustomerId())
                .build());
    }
}
