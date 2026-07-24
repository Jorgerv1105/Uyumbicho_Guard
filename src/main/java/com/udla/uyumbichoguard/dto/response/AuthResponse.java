package com.udla.uyumbichoguard.dto.response;

/**
 * Respuesta del login/refresh. NUNCA incluye la entidad Usuario
 * completa (que tiene el hash de password) — solo los datos que el
 * frontend necesita para la sesión.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        UsuarioResponse usuario
) {
    public AuthResponse(String accessToken, String refreshToken, UsuarioResponse usuario) {
        this(accessToken, refreshToken, "Bearer", usuario);
    }
}