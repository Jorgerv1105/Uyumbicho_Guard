package com.udla.uyumbichoguard.provider.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de conexión al servicio real del SRI/ANT, mapeadas
 * desde application.yml (prefijo app.vehiculo-provider.sri).
 * Los valores reales (URL, API key) se inyectan por variable de
 * entorno en producción — NUNCA se versionan en el repositorio
 * (ver .env.example en Parte 13).
 */
@Component
@ConfigurationProperties(prefix = "app.vehiculo-provider.sri")
@Getter
@Setter
public class SriProviderProperties {

    /** URL base de la API REST del SRI/ANT. */
    private String baseUrl;

    /** API key / token de autenticación provisto por el SRI. */
    private String apiKey;

    /** Timeout de conexión en milisegundos. */
    private int timeoutConexionMs = 5000;

    /** Timeout de lectura en milisegundos. */
    private int timeoutLecturaMs = 8000;
}