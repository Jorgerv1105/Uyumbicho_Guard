package com.udla.uyumbichoguard.notificacion.dto;

import java.util.List;

/**
 * Representa el payload exacto que espera la Graph API de Meta para
 * enviar un mensaje de plantilla. Estructura oficial:
 * https://developers.facebook.com/docs/whatsapp/cloud-api/reference/messages
 */
public record WhatsappTemplateRequest(
        String messaging_product,
        String to,
        String type,
        Template template
) {
    public record Template(String name, Language language, List<Component> components) {}
    public record Language(String code) {}
    public record Component(String type, List<Parameter> parameters) {}
    public record Parameter(String type, String text) {}

    /** Construye el request para un mensaje de plantilla con parámetros
     * de texto simples en el cuerpo (caso de uso de esta app). */
    public static WhatsappTemplateRequest de(String telefono, String nombrePlantilla, String idioma, List<String> parametros) {
        List<Parameter> params = parametros.stream()
                .map(p -> new Parameter("text", p))
                .toList();

        return new WhatsappTemplateRequest(
                "whatsapp",
                telefono,
                "template",
                new Template(nombrePlantilla, new Language(idioma), List.of(new Component("body", params)))
        );
    }
}