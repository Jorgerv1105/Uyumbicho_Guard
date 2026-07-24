package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.RegistroAcceso;
import com.udla.uyumbichoguard.model.enums.OrigenRegistro;
import com.udla.uyumbichoguard.model.enums.TipoMovimiento;

import java.time.LocalDateTime;

public record RegistroAccesoResponse(
        Long id,
        String placa,
        Long vehiculoId,
        TipoMovimiento tipoMovimiento,
        LocalDateTime fechaHora,
        String vigilanteNombre,
        String fotoPlacaUrl,
        OrigenRegistro origen,
        Double confianzaOcr,
        String observaciones
) {
    public static RegistroAccesoResponse desde(RegistroAcceso registro) {
        return new RegistroAccesoResponse(
                registro.getId(),
                registro.getPlaca(),
                registro.getVehiculo() != null ? registro.getVehiculo().getId() : null,
                registro.getTipoMovimiento(),
                registro.getFechaHora(),
                registro.getVigilante().getNombres() + " " + registro.getVigilante().getApellidos(),
                registro.getFotoPlacaUrl(),
                registro.getOrigen(),
                registro.getConfianzaOcr(),
                registro.getObservaciones()
        );
    }
}