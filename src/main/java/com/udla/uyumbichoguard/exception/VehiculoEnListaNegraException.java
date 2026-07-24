package com.udla.uyumbichoguard.exception;

/**
 * Se lanza al intentar registrar el ingreso de un vehículo cuya placa
 * está activa en la lista negra (Parte 5, RegistroService). No impide
 * necesariamente el registro (el vigilante puede decidir dejar
 * evidencia igual), pero permite a los controllers reaccionar
 * (ej: generar la AlertaSeguridad correspondiente) de forma explícita.
 * Mapeada a HTTP 409 (Conflict) — informativa, no un error de sistema.
 */
public class VehiculoEnListaNegraException extends RuntimeException {
    public VehiculoEnListaNegraException(String mensaje) {
        super(mensaje);
    }
}