package com.udla.uyumbichoguard.model.enums;

/**
 * PENDIENTE: registro creado, aún no se intenta el envío (estado transitorio).
 * ENVIADO: Meta confirmó recepción (se guarda el messageId de respuesta).
 * FALLIDO: Meta rechazó el envío o hubo error de red.
 * OMITIDO: app.whatsapp.enabled=false (desarrollo sin credenciales reales)
 *          o el destinatario no tiene teléfono registrado.
 */
public enum EstadoNotificacionWhatsapp {
    PENDIENTE,
    ENVIADO,
    FALLIDO,
    OMITIDO
}