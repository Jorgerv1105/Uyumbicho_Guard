package com.udla.uyumbichoguard.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración JWT, mapeadas desde application.yml
 * (prefijo app.jwt). Se centralizan aquí en vez de usar @Value disperso
 * para tener un único punto de verdad y facilitar testing.
 *
 * IMPORTANTE (seguridad): el secreto (app.jwt.secret) NUNCA debe ir
 * hardcodeado ni versionado en el repositorio. Se inyecta por variable
 * de entorno en producción (ver Parte 13, docker-compose + .env).
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secreto en Base64 usado para firmar tokens con HMAC-SHA256.
     * Debe tener al menos 256 bits (32 bytes) una vez decodificado,
     * requisito del algoritmo HS256 para ser criptográficamente seguro.
     */
    private String secret;

    /**
     * Tiempo de vida del access token en milisegundos.
     * Corto a propósito (ej: 15 min) para minimizar la ventana de
     * riesgo si un token es interceptado.
     */
    private long expirationMs;

    /**
     * Tiempo de vida del refresh token en milisegundos.
     * Más largo (ej: 7 días), permite renovar el access token sin
     * pedir credenciales de nuevo.
     */
    private long refreshExpirationMs;
}