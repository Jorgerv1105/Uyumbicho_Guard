package com.udla.uyumbichoguard.controller;

import com.udla.uyumbichoguard.dto.request.LoginRequest;
import com.udla.uyumbichoguard.dto.request.RefreshTokenRequest;
import com.udla.uyumbichoguard.dto.response.AuthResponse;
import com.udla.uyumbichoguard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticación (permitAll en SecurityConfig,
 * Parte 2). El rate limiting de /api/auth/login ya está aplicado a
 * nivel de filtro (RateLimitingFilter, Parte 2), no aquí.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}