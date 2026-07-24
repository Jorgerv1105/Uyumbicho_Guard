package com.udla.uyumbichoguard.provider.dto;

/**
 * Representa la información vehicular tal como la devuelve una fuente
 * externa (SRI/ANT), desacoplada de nuestra entidad Vehiculo.
 *
 * Por qué un DTO propio y no reusar Vehiculo: el SRI puede devolver
 * campos que no nos interesan (ej: estado de impuestos, aseguradora),
 * y nuestra entidad tiene campos que el SRI no conoce (residenteId,
 * esVisitante). Mezclar ambos acoplaría nuestro dominio a la forma
 * exacta de la respuesta externa, que puede cambiar sin aviso.
 */
public record InfoVehiculoExterna(
        String placa,
        String marca,
        String modelo,
        String color,
        Integer anioFabricacion,
        String tipoVehiculo,
        String nombrePropietario,
        String cedulaPropietario,
        boolean matriculaVigente,
        boolean tienePendientesLegales
) {}