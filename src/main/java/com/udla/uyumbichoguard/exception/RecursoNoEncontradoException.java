package com.udla.uyumbichoguard.exception;

/**
 * Se lanza cuando se busca una entidad por ID/campo único y no existe.
 * Mapeada a HTTP 404 en GlobalExceptionHandler.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}