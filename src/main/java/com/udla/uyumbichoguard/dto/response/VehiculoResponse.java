package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.Vehiculo;
import com.udla.uyumbichoguard.model.enums.TipoVehiculo;

public record VehiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        String color,
        TipoVehiculo tipo,
        Integer anioFabricacion,
        Long residenteId,
        String residenteNombre,
        Boolean esVisitante,
        Boolean activo
) {
    public static VehiculoResponse desde(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getColor(),
                vehiculo.getTipo(),
                vehiculo.getAnioFabricacion(),
                vehiculo.getResidente() != null ? vehiculo.getResidente().getId() : null,
                vehiculo.getResidente() != null ? vehiculo.getResidente().getNombresCompletos() : null,
                vehiculo.getEsVisitante(),
                vehiculo.getActivo()
        );
    }
}