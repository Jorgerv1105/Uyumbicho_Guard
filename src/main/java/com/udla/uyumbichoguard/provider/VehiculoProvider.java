package com.udla.uyumbichoguard.provider;

import com.udla.uyumbichoguard.provider.dto.InfoVehiculoExterna;

import java.util.Optional;

/**
 * PUERTO del patrón Adapter: contrato único que el resto de la
 * aplicación (RegistroService, Parte 5) usa para consultar datos
 * vehiculares externos, sin saber ni importarle si por debajo hay un
 * mock o la API real del SRI.
 *
 * Esto permite:
 * 1. Desarrollar y demostrar la tesis sin depender de credenciales
 *    reales del SRI (MockVehiculoProvider).
 * 2. Cambiar a producción real cambiando SOLO application.yml
 *    (app.vehiculo-provider.tipo=sri), sin tocar ni una línea de los
 *    services que consumen esta interfaz.
 * 3. Facilitar testing unitario del resto del sistema con un mock de
 *    esta interfaz, sin llamadas HTTP reales.
 */
public interface VehiculoProvider {

    /**
     * Consulta los datos de un vehículo por placa en la fuente externa.
     * Optional.empty() si la placa no existe en el registro externo
     * (NO es un error — es información válida: "esta placa no está
     * matriculada").
     *
     * @throws com.udla.uyumbichoguard.provider.exception.VehiculoProviderException
     *         si el proveedor externo falla (timeout, error 5xx, etc.)
     */
    Optional<InfoVehiculoExterna> consultarPorPlaca(String placa);

    /**
     * Nombre identificador del proveedor activo, útil para logging y
     * para mostrar en el frontend de qué fuente vino el dato
     * (ej: "Mock (desarrollo)" vs "SRI/ANT Ecuador").
     */
    String nombreProveedor();
}