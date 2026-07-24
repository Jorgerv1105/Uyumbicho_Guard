package com.udla.uyumbichoguard.exception;

/**
 * Se lanza al intentar crear un recurso que viola una restricción de
 * unicidad de negocio (email, cédula, placa duplicada, etc.).
 * Mapeada a HTTP 409 (Conflict) en GlobalExceptionHandler.
 */
public class RecursoDuplicadoException extends RuntimeException {
    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}