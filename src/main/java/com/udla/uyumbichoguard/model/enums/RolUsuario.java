package com.udla.uyumbichoguard.model.enums;

/**
 * Roles del sistema UyumbichoGuard.
 * Se usan tal cual (con prefijo ROLE_ agregado en tiempo de ejecución
 * por Spring Security, no aquí) para autorización basada en roles.
 */
public enum RolUsuario {
    ADMIN,
    SUPERVISOR,
    VIGILANTE,
    RESIDENTE
}