package com.udla.uyumbichoguard.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Se invoca cuando una request no autenticada intenta acceder a un
 * recurso protegido. Devuelve un JSON uniforme en vez de la página de
 * error HTML por defecto de Spring Security — importante porque el
 * frontend (React) espera siempre JSON de la API.
 *
 * Deliberadamente NO se expone el detalle interno de
 * AuthenticationException (authException.getMessage()) en la
 * respuesta: evita revelar información sobre por qué falló la
 * autenticación (token expirado vs inválido vs ausente), que podría
 * ayudar a un atacante a afinar su ataque.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("""
                {"timestamp":"%s","status":401,"error":"No autorizado","message":"Debes iniciar sesión para acceder a este recurso"}
                """.formatted(LocalDateTime.now()));
    }
}