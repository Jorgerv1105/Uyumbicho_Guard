package com.udla.uyumbichoguard.notificacion;

/**
 * Encapsula cualquier fallo al comunicarse con Meta (credenciales
 * inválidas, plantilla no aprobada, número inválido, timeout, etc.).
 * SIEMPRE se captura dentro de NotificacionWhatsappService — nunca debe
 * propagarse y afectar la transacción de negocio que disparó el envío.
 */
public class WhatsappEnvioException extends RuntimeException {
    public WhatsappEnvioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public WhatsappEnvioException(String mensaje) {
        super(mensaje);
    }
}