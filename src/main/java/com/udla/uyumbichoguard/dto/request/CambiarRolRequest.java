package com.udla.uyumbichoguard.dto.request;

import com.udla.uyumbichoguard.model.enums.RolUsuario;
import jakarta.validation.constraints.NotNull;

/** Endpoint separado y restringido solo a ADMIN (ver Parte 6). */
public record CambiarRolRequest(
        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol
) {}