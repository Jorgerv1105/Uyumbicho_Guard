package com.udla.uyumbichoguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearResidenteRequest(

        @NotBlank(message = "Los nombres completos son obligatorios")
        @Size(max = 150, message = "Los nombres no pueden superar 150 caracteres")
        String nombresCompletos,

        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cédula debe tener exactamente 10 dígitos")
        String cedula,

        @Pattern(regexp = "\\d{7,10}", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String telefonoContacto,

        @NotBlank(message = "La manzana es obligatoria")
        @Size(max = 20, message = "La manzana no puede superar 20 caracteres")
        String manzana,

        @NotBlank(message = "El número de casa es obligatorio")
        @Size(max = 20, message = "El número de casa no puede superar 20 caracteres")
        String numeroCasa,

        @Size(max = 255, message = "La referencia no puede superar 255 caracteres")
        String direccionReferencia
) {}