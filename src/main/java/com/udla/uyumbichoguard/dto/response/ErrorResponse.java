package com.udla.uyumbichoguard.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato uniforme de error para TODA la API. Se usa tanto aquí
 * (GlobalExceptionHandler) como en JwtAuthenticationEntryPoint
 * (Parte 2) para que el frontend maneje errores de forma consistente
 * sin importar en qué capa se originaron.
 *
 * Record en vez de clase: es inmutable por naturaleza, apropiado para
 * un DTO de solo salida que no necesita builders ni setters.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> detalles
) {
    public ErrorResponse(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    public ErrorResponse(int status, String error, String message, List<String> detalles) {
        this(LocalDateTime.now(), status, error, message, detalles);
    }
}