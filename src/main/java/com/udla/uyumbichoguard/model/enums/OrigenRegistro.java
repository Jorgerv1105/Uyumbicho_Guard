package com.udla.uyumbichoguard.model.enums;

/**
 * Indica si el registro de acceso se generó por reconocimiento automático
 * (OCR sobre la placa) o fue digitado manualmente por el vigilante.
 * Es clave para auditoría: un registro MANUAL es más propenso a error
 * humano y puede requerir validación adicional.
 */
public enum OrigenRegistro {
    MANUAL,
    OCR_AUTOMATICO
}