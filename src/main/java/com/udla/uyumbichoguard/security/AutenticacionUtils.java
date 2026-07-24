package com.udla.uyumbichoguard.security;

import com.udla.uyumbichoguard.model.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Obtiene el Usuario autenticado actual desde el SecurityContext.
 *
 * Funciona directamente porque Usuario implementa UserDetails (Parte 1)
 * y JwtAuthenticationFilter (Parte 2) coloca la entidad completa como
 * principal del Authentication — no un DTO ni un String — evitando así
 * una consulta extra a la base de datos en cada service que necesita
 * saber "quién hizo esta acción".
 */
public final class AutenticacionUtils {

    private AutenticacionUtils() {
        // Clase de utilidades, no instanciable.
    }

    public static Usuario usuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        // No debería ocurrir en un endpoint protegido correctamente
        // configurado; es una red de seguridad ante errores de config.
        throw new IllegalStateException("No hay un usuario autenticado válido en el contexto de seguridad");
    }
}