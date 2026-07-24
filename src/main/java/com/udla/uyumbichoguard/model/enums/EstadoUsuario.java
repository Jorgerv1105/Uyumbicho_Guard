package com.udla.uyumbichoguard.model.enums;

/**
 * Estado de la cuenta de un Usuario.
 * BLOQUEADO se usa para el mecanismo de bloqueo por intentos fallidos
 * de login (protección contra fuerza bruta), independiente de si el
 * ADMIN desactivó la cuenta manualmente (INACTIVO).
 */
public enum EstadoUsuario {
    ACTIVO,
    INACTIVO,
    BLOQUEADO
}