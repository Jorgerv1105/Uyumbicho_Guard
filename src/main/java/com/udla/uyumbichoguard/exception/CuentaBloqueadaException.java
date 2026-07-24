package com.udla.uyumbichoguard.exception;

/**
 * Se lanza específicamente cuando la cuenta está bloqueada (por
 * intentos fallidos o por decisión del ADMIN). A diferencia de
 * CredencialesInvalidasException, este SÍ informa al usuario legítimo
 * el motivo exacto (no compromete seguridad, ya que solo el dueño de
 * la cuenta -que ya conoce su propio email- puede triggerearlo tras
 * varios intentos).
 * Mapeada a HTTP 423 (Locked) en GlobalExceptionHandler.
 */
public class CuentaBloqueadaException extends RuntimeException {
    public CuentaBloqueadaException(String mensaje) {
        super(mensaje);
    }
}