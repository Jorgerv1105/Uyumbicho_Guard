package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.Usuario;
import com.udla.uyumbichoguard.model.enums.EstadoUsuario;
import com.udla.uyumbichoguard.model.enums.RolUsuario;

import java.time.LocalDateTime;

/**
 * Vista pública de Usuario para respuestas de la API.
 * Excluye deliberadamente: password (hash), intentosFallidos,
 * fechaBloqueoHasta — son detalles internos de seguridad que no debe
 * ver ni siquiera el propio usuario autenticado vía la API normal.
 */
public record UsuarioResponse(
        Long id,
        String cedula,
        String nombres,
        String apellidos,
        String email,
        String telefono,
        RolUsuario rol,
        EstadoUsuario estado,
        LocalDateTime ultimoAcceso,
        LocalDateTime createdAt
) {
    /** Factory method: mapeo explícito campo a campo (sin librerías
     * de mapping como MapStruct, para mantener el stack mínimo y que
     * sea fácil de auditar en la defensa de tesis qué campos se
     * exponen exactamente). */
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getCedula(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getUltimoAcceso(),
                usuario.getCreatedAt()
        );
    }
}