package com.udla.uyumbichoguard.provider;

import com.udla.uyumbichoguard.provider.dto.InfoVehiculoExterna;
import com.udla.uyumbichoguard.provider.dto.SriVehiculoApiResponse;
import com.udla.uyumbichoguard.provider.exception.VehiculoProviderException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

/**
 * ADAPTER concreto #2 (implementación de producción): consume la API
 * REST real del SRI/ANT Ecuador para obtener datos vehiculares.
 *
 * Se activa SOLO con el perfil "sri" (application.yml:
 * app.vehiculo-provider.tipo=sri), típicamente en el ambiente de
 * producción/despliegue final, nunca en desarrollo local.
 *
 * IMPORTANTE para la defensa de tesis: esta clase queda con la
 * estructura completa y correcta del patrón Adapter, lista para
 * conectar. El endpoint exacto (/vehiculos/consulta) y el mapeo de
 * SriVehiculoApiResponse deben ajustarse a la documentación oficial
 * una vez la institución otorgue las credenciales de acceso a la API
 * del SRI — esto está fuera del control del desarrollo de software y
 * depende de un trámite institucional con el SRI/ANT.
 */
@Component
@Profile("sri")
@RequiredArgsConstructor
public class SriAntVehiculoProvider implements VehiculoProvider {

    private static final Logger log = LoggerFactory.getLogger(SriAntVehiculoProvider.class);

    private final RestClient sriRestClient;

    @Override
    public Optional<InfoVehiculoExterna> consultarPorPlaca(String placa) {
        String placaNormalizada = placa.trim().toUpperCase().replace("-", "");

        try {
            SriVehiculoApiResponse respuesta = sriRestClient.get()
                    .uri("/vehiculos/consulta/{placa}", placaNormalizada)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // 404 del SRI = placa no matriculada, no es un
                        // error de nuestro sistema; se maneja como
                        // Optional.empty() más abajo mediante el catch
                        // específico de RestClientResponseException.
                        if (response.getStatusCode().value() != 404) {
                            throw new VehiculoProviderException(
                                    "El SRI rechazó la consulta (código %d)".formatted(response.getStatusCode().value())
                            );
                        }
                    })
                    .body(SriVehiculoApiResponse.class);

            if (respuesta == null) {
                return Optional.empty();
            }

            return Optional.of(mapearAInfoVehiculoExterna(respuesta));

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                log.info("Placa {} no encontrada en el registro del SRI", placaNormalizada);
                return Optional.empty();
            }
            log.error("Error de respuesta del SRI para placa {}: {}", placaNormalizada, ex.getStatusCode());
            throw new VehiculoProviderException(
                    "El servicio del SRI no está disponible en este momento", ex
            );
        } catch (Exception ex) {
            log.error("Error de conexión con el SRI para placa {}", placaNormalizada, ex);
            throw new VehiculoProviderException(
                    "No se pudo conectar con el servicio del SRI", ex
            );
        }
    }

    @Override
    public String nombreProveedor() {
        return "SRI/ANT Ecuador (producción)";
    }

    /**
     * Traduce la forma cruda de la respuesta del SRI a nuestro DTO de
     * dominio. Punto único de mapeo — si el contrato del SRI cambia,
     * solo se ajusta aquí.
     */
    private InfoVehiculoExterna mapearAInfoVehiculoExterna(SriVehiculoApiResponse r) {
        return new InfoVehiculoExterna(
                r.numeroPlaca(),
                r.marca(),
                r.modelo(),
                r.colorVehiculo(),
                r.anioModelo(),
                r.claseVehiculo(),
                r.propietarioNombre(),
                r.propietarioCedula(),
                "VIGENTE".equalsIgnoreCase(r.estadoMatricula()),
                Boolean.TRUE.equals(r.tienePendientes())
        );
    }
}