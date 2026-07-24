package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.NotificacionWhatsapp;
import com.udla.uyumbichoguard.model.enums.EstadoNotificacionWhatsapp;
import com.udla.uyumbichoguard.model.enums.TipoNotificacionWhatsapp;

import java.time.LocalDateTime;

public record NotificacionWhatsappResponse(
        Long id,
        TipoNotificacionWhatsapp tipo,
        String telefonoDestino,
        String nombreDestino,
        String mensajeResumen,
        EstadoNotificacionWhatsapp estado,
        String errorDetalle,
        LocalDateTime fechaEnvio,
        LocalDateTime createdAt
) {
    public static NotificacionWhatsappResponse desde(NotificacionWhatsapp n) {
        return new NotificacionWhatsappResponse(
                n.getId(),
                n.getTipo(),
                n.getTelefonoDestino(),
                n.getNombreDestino(),
                n.getMensajeResumen(),
                n.getEstado(),
                n.getErrorDetalle(),
                n.getFechaEnvio(),
                n.getCreatedAt()
        );
    }
}