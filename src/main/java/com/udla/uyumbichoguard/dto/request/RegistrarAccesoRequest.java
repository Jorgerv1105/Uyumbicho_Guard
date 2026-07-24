package com.udla.uyumbichoguard.dto.request;

import com.udla.uyumbichoguard.model.enums.OrigenRegistro;
import com.udla.uyumbichoguard.model.enums.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarAccesoRequest(

        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        @NotNull(message = "El origen del registro es obligatorio")
        OrigenRegistro origen,

        String fotoPlacaUrl,

        Double confianzaOcr,

        String observaciones
) {}