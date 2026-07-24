package com.udla.uyumbichoguard.provider.exception;

/**
 * Excepción de infraestructura: se lanza cuando el proveedor externo
 * (SRI/ANT) no responde, responde con error, o el formato es
 * inesperado. Se distingue de RecursoNoEncontradoException (Parte 3)
 * porque esto es un fallo de INFRAESTRUCTURA (el servicio externo
 * falló), no una regla de negocio nuestra.
 *
 * El RegistroService (Parte 5) decide cómo reaccionar: normalmente
 * degradando con gracia (permitir registro manual) en vez de bloquear
 * toda la operación de la garita por una caída del SRI.
 */
public class VehiculoProviderException extends RuntimeException {
    public VehiculoProviderException(String mensaje) {
        super(mensaje);
    }

    public VehiculoProviderException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}