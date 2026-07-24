package com.udla.uyumbichoguard.dto.response;

import java.time.LocalDateTime;

/**
 * Representa un vehículo actualmente DENTRO del pueblo (su último
 * movimiento fue ENTRADA sin SALIDA posterior). Usado en el dashboard
 * de la Parte 10.
 */
public record VehiculoActivoResponse(
        String placa,
        String residenteNombre,
        String manzana,
        String numeroCasa,
        LocalDateTime horaIngreso,
        Boolean esVisitante
) {}