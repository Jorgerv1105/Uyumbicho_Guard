package com.udla.uyumbichoguard.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Campos actualizables de un usuario existente. Deliberadamente NO
 * incluye email, cédula ni rol: cambiar el email/cédula requeriría
 * re-verificación (fuera de alcance de la tesis), y el rol se cambia
 * por un endpoint separado con autorización más estricta (solo ADMIN,
 * ver Parte 6) para no mezclar "editar mi perfil" con "escalar
 * privilegios" en el mismo request.
 */
public record ActualizarUsuarioRequest(

        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
        String nombres,

        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String apellidos,

        @Pattern(regexp = "\\d{7,10}", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String telefono
) {}