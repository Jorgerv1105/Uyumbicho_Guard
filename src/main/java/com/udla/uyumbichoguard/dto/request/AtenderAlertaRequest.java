package com.udla.uyumbichoguard.dto.request;

import jakarta.validation.constraints.Size;

public record AtenderAlertaRequest(
        @Size(max = 255, message = "La observación no puede superar 255 caracteres")
        String observacion
) {}