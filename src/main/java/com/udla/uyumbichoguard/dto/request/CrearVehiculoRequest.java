package com.udla.uyumbichoguard.dto.request;

import com.udla.uyumbichoguard.model.enums.TipoVehiculo;
import jakarta.validation.constraints.*;

public record CrearVehiculoRequest(

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(
                regexp = "^[A-Z]{2,3}-?\\d{3,4}$",
                message = "La placa debe tener el formato ecuatoriano válido (ej: PBX-1234)"
        )
        String placa,

        @Size(max = 50, message = "La marca no puede superar 50 caracteres")
        String marca,

        @Size(max = 50, message = "El modelo no puede superar 50 caracteres")
        String modelo,

        @Size(max = 30, message = "El color no puede superar 30 caracteres")
        String color,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        TipoVehiculo tipo,

        @Min(value = 1980, message = "El año de fabricación no es válido")
        @Max(value = 2100, message = "El año de fabricación no es válido")
        Integer anioFabricacion,

        Long residenteId,

        @NotNull(message = "Debe indicar si es un vehículo de visitante")
        Boolean esVisitante
) {}