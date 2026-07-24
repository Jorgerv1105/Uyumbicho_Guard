package com.udla.uyumbichoguard.dto.request;

import com.udla.uyumbichoguard.model.enums.RolUsuario;
import jakarta.validation.constraints.*;

public record CrearUsuarioRequest(

        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cédula debe tener exactamente 10 dígitos")
        String cedula,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe incluir mayúsculas, minúsculas y números"
        )
        String password,

        @Pattern(regexp = "\\d{7,10}", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String telefono,

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol
) {}