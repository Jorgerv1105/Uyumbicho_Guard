package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.AlertaSeguridad;
import com.udla.uyumbichoguard.model.enums.NivelAlerta;
import com.udla.uyumbichoguard.model.enums.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaSeguridadResponse(
        Long id,
        TipoAlerta tipo,
        NivelAlerta nivel,
        String descripcion,
        LocalDateTime fechaHora,
        Long registroAccesoId,
        Boolean atendida,
        String atendidaPorNombre,
        LocalDateTime fechaAtencion
) {
    public static AlertaSeguridadResponse desde(AlertaSeguridad alerta) {
        return new AlertaSeguridadResponse(
                alerta.getId(),
                alerta.getTipo(),
                alerta.getNivel(),
                alerta.getDescripcion(),
                alerta.getFechaHora(),
                alerta.getRegistroAcceso() != null ? alerta.getRegistroAcceso().getId() : null,
                alerta.getAtendida(),
                alerta.getAtendidaPor() != null
                        ? alerta.getAtendidaPor().getNombres() + " " + alerta.getAtendidaPor().getApellidos()
                        : null,
                alerta.getFechaAtencion()
        );
    }
}