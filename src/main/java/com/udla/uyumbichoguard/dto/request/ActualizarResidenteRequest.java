package com.udla.uyumbichoguard.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarResidenteRequest(

        @Size(max = 150, message = "Los nombres no pueden superar 150 caracteres")
        String nombresCompletos,

        @Pattern(regexp = "\\d{7,10}", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String telefonoContacto,

        @Size(max = 20, message = "La manzana no puede superar 20 caracteres")
        String manzana,

        @Size(max = 20, message = "El número de casa no puede superar 20 caracteres")
        String numeroCasa,

        @Size(max = 255, message = "La referencia no puede superar 255 caracteres")
        String direccionReferencia
) {}