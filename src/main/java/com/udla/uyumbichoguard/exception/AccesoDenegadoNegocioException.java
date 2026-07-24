package com.udla.uyumbichoguard.exception;

/**
 * Para reglas de negocio de autorización que van más allá de lo que
 * @PreAuthorize puede expresar declarativamente (ej: un SUPERVISOR
 * intentando modificar a un ADMIN). Mapeada a HTTP 403.
 */
public class AccesoDenegadoNegocioException extends RuntimeException {
    public AccesoDenegadoNegocioException(String mensaje) {
        super(mensaje);
    }
}