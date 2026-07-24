package com.udla.uyumbichoguard.provider;

import com.udla.uyumbichoguard.provider.dto.InfoVehiculoExterna;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementación por defecto (ADAPTER concreto #1): simula respuestas
 * del SRI/ANT sin depender de conectividad externa real.
 *
 * Es la implementación ACTIVA por defecto (@Profile con "default"
 * incluido) porque el stack definido para la tesis dice
 * explícitamente "MockVehiculoProvider por defecto". Se activa cuando
 * app.vehiculo-provider.tipo=mock O cuando no se especifica el
 * profile (fallback seguro para no romper el arranque si se olvida
 * configurar).
 *
 * Comportamiento determinista (mismo resultado para la misma placa
 * siempre) usando el hash de la placa, para que las demos y pruebas
 * de la defensa de tesis sean reproducibles y no aleatorias.
 */
@Component
@ConditionalOnProperty(
        prefix = "app.vehiculo-provider",
        name = "tipo",
        havingValue = "mock",
        matchIfMissing = true // Si no se define la propiedad, Mock es el default seguro.
)
public class MockVehiculoProvider implements VehiculoProvider {

    private static final Logger log = LoggerFactory.getLogger(MockVehiculoProvider.class);

    private static final List<String> MARCAS = List.of("Chevrolet", "Toyota", "Kia", "Hyundai", "Nissan", "Mazda");
    private static final List<String> MODELOS = List.of("Sail", "Yaris", "Rio", "Accent", "Sentra", "CX-5");
    private static final List<String> COLORES = List.of("Blanco", "Gris", "Negro", "Rojo", "Azul", "Plata");
    private static final List<String> TIPOS = List.of("AUTOMOVIL", "CAMIONETA", "MOTOCICLETA");

    /**
     * Placas reservadas que simulan casos específicos útiles para la
     * demo de tesis: una placa "sin resultados" y una con "pendientes
     * legales", para poder mostrar ambos flujos sin depender del azar.
     */
    private static final String PLACA_DEMO_NO_ENCONTRADA = "ZZZ9999";
    private static final String PLACA_DEMO_CON_PENDIENTES = "ABC1234";

    @Override
    public Optional<InfoVehiculoExterna> consultarPorPlaca(String placa) {
        String placaNormalizada = normalizar(placa);
        log.debug("[MockVehiculoProvider] Consultando placa simulada: {}", placaNormalizada);

        if (placaNormalizada.equals(PLACA_DEMO_NO_ENCONTRADA)) {
            return Optional.empty();
        }

        int semilla = Math.abs(placaNormalizada.hashCode());

        InfoVehiculoExterna info = new InfoVehiculoExterna(
                placaNormalizada,
                MARCAS.get(semilla % MARCAS.size()),
                MODELOS.get(semilla % MODELOS.size()),
                COLORES.get(semilla % COLORES.size()),
                2015 + (semilla % 10),
                TIPOS.get(semilla % TIPOS.size()),
                "PROPIETARIO SIMULADO " + placaNormalizada,
                generarCedulaSimulada(semilla),
                true,
                placaNormalizada.equals(PLACA_DEMO_CON_PENDIENTES)
        );

        return Optional.of(info);
    }

    @Override
    public String nombreProveedor() {
        return "Mock (entorno de desarrollo/demostración)";
    }

    private String normalizar(String placa) {
        return placa.trim().toUpperCase().replace("-", "");
    }

    /** Genera una cédula ecuatoriana simulada consistente con formato
     * (10 dígitos), no necesita pasar el algoritmo de validación real
     * del dígito verificador ya que es solo para datos de demostración. */
    private String generarCedulaSimulada(int semilla) {
        int base = 1000000000 + (semilla % 900000000);
        return String.valueOf(base);
    }
}