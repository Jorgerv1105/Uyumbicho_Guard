package com.udla.uyumbichoguard.notificacion;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración de la integración con Meta WhatsApp Cloud API,
 * mapeada desde application.yml (prefijo app.whatsapp).
 *
 * IMPORTANTE (seguridad): accessToken NUNCA se versiona en el
 * repositorio — se inyecta por variable de entorno en producción
 * (ver Parte 13, docker-compose + .env).
 */
@Component
@ConfigurationProperties(prefix = "app.whatsapp")
@Getter
@Setter
public class WhatsappProperties {

    /** Interruptor maestro: false = no se llama a Meta, todo queda OMITIDO.
     * Útil en desarrollo mientras no haya cuenta de Meta Business verificada. */
    private boolean enabled = false;

    private String baseUrl = "https://graph.facebook.com";
    private String apiVersion = "v20.0";

    /** ID del número de teléfono de WhatsApp Business (Meta Business Manager). */
    private String phoneNumberId;

    /** Token de acceso permanente de la app de Meta. */
    private String accessToken;

    /** Nombre de la plantilla aprobada para alertas de seguridad. */
    private String plantillaAlertaSeguridad = "alerta_seguridad_uyumbichoguard";

    /** Nombre de la plantilla aprobada para notificar ingreso/salida de vehículo. */
    private String plantillaMovimientoVehiculo = "notificacion_movimiento_vehiculo";

    /** Código de idioma de las plantillas registradas en Meta. */
    private String idiomaPlantilla = "es";
}