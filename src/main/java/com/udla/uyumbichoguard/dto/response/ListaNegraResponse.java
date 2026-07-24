package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.ListaNegra;

import java.time.LocalDateTime;

public record ListaNegraResponse(
        Long id,
        String placa,
        String motivo,
        String registradoPorNombre,
        LocalDateTime fechaExpiracion,
        Boolean activo,
        LocalDateTime createdAt
) {
    public static ListaNegraResponse desde(ListaNegra listaNegra) {
        return new ListaNegraResponse(
                listaNegra.getId(),
                listaNegra.getPlaca(),
                listaNegra.getMotivo(),
                listaNegra.getRegistradoPor().getNombres() + " " + listaNegra.getRegistradoPor().getApellidos(),
                listaNegra.getFechaExpiracion(),
                listaNegra.getActivo(),
                listaNegra.getCreatedAt()
        );
    }
}