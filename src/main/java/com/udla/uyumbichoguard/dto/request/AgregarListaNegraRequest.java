package com.udla.uyumbichoguard.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AgregarListaNegraRequest(

        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
        String motivo,

        @Future(message = "La fecha de expiración debe ser futura")
        LocalDateTime fechaExpiracion
) {}