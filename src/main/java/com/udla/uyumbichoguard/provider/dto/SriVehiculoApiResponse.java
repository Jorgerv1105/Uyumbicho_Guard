package com.udla.uyumbichoguard.provider.dto;

/**
 * Representa la forma cruda (tal cual) de la respuesta JSON de la API
 * del SRI/ANT. Se mapea a InfoVehiculoExterna en SriAntVehiculoProvider
 * antes de devolverla al resto del sistema — así si el SRI cambia su
 * contrato JSON, solo se toca esta clase y el método de mapeo, nunca
 * el resto de la aplicación.
 *
 * NOTA: la estructura exacta de campos de la API real del SRI debe
 * ajustarse aquí una vez se tengan las credenciales y documentación
 * oficial de acceso (trámite institucional pendiente para el
 * despliegue final de la tesis). Esta es una estructura razonable
 * basada en el formato típico de APIs de consulta vehicular del SRI.
 */
public record SriVehiculoApiResponse(
        String numeroPlaca,
        String marca,
        String modelo,
        String colorVehiculo,
        Integer anioModelo,
        String claseVehiculo,
        String propietarioNombre,
        String propietarioCedula,
        String estadoMatricula,
        Boolean tienePendientes
) {}