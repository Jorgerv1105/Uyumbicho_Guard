package com.udla.uyumbichoguard.dto.request;

import com.udla.uyumbichoguard.model.enums.TipoVehiculo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ActualizarVehiculoRequest(

        @Size(max = 50, message = "La marca no puede superar 50 caracteres")
        String marca,

        @Size(max = 50, message = "El modelo no puede superar 50 caracteres")
        String modelo,

        @Size(max = 30, message = "El color no puede superar 30 caracteres")
        String color,

        TipoVehiculo tipo,

        @Min(value = 1980, message = "El año de fabricación no es válido")
        @Max(value = 2100, message = "El año de fabricación no es válido")
        Integer anioFabricacion
) {}