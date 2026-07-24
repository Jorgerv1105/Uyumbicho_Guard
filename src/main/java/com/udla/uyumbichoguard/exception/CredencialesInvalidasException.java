package com.udla.uyumbichoguard.exception;

/**
 * Se lanza en AuthService cuando el login falla (email no existe,
 * password incorrecta, o cuenta bloqueada/inactiva).
 *
 * Decisión de seguridad: se usa UN SOLO mensaje genérico para todos
 * estos casos distintos (ver AuthService en Parte 5) para no revelar
 * a un atacante si un email específico está registrado en el sistema
 * (mitiga ataques de enumeración de usuarios).
 * Mapeada a HTTP 401 en GlobalExceptionHandler.
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}