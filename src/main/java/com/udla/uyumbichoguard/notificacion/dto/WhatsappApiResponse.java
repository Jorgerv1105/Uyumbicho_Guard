package com.udla.uyumbichoguard.notificacion.dto;

import java.util.List;

/** Respuesta exitosa de Meta al enviar un mensaje. */
public record WhatsappApiResponse(
        String messaging_product,
        List<Mensaje> messages
) {
    public record Mensaje(String id) {}
}