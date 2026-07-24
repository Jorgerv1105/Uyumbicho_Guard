package com.udla.uyumbichoguard.notificacion;

import com.udla.uyumbichoguard.model.*;
import com.udla.uyumbichoguard.model.enums.EstadoNotificacionWhatsapp;
import com.udla.uyumbichoguard.model.enums.RolUsuario;
import com.udla.uyumbichoguard.model.enums.TipoMovimiento;
import com.udla.uyumbichoguard.model.enums.TipoNotificacionWhatsapp;
import com.udla.uyumbichoguard.notificacion.dto.WhatsappApiResponse;
import com.udla.uyumbichoguard.notificacion.dto.WhatsappTemplateRequest;
import com.udla.uyumbichoguard.repository.NotificacionWhatsappRepository;
import com.udla.uyumbichoguard.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Punto único de envío de notificaciones WhatsApp vía Meta Cloud API.
 *
 * Decisión de diseño clave: todos los métodos públicos son @Async y
 * NUNCA lanzan excepción hacia quien los invoca (SeguridadService,
 * RegistroService) — cualquier fallo de Meta queda registrado en la
 * tabla NotificacionWhatsapp con estado FALLIDO, pero jamás revierte
 * ni afecta la operación de negocio principal (registrar un ingreso
 * o generar una alerta es más importante que el WhatsApp llegue).
 */
@Service
@RequiredArgsConstructor
public class NotificacionWhatsappService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionWhatsappService.class);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RestClient whatsappRestClient;
    private final WhatsappProperties whatsappProperties;
    private final NotificacionWhatsappRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Notifica a todos los ADMIN/SUPERVISOR con teléfono registrado
     * cuando ocurre una alerta de seguridad crítica (vehículo en lista
     * negra o placa no registrada intentando ingresar).
     */
    @Async("whatsappTaskExecutor")
    public void notificarAlertaSeguridad(AlertaSeguridad alerta) {
        List<Usuario> destinatarios = usuarioRepository.findByRol(RolUsuario.ADMIN);
        destinatarios.addAll(usuarioRepository.findByRol(RolUsuario.SUPERVISOR));

        String plantilla = whatsappProperties.getPlantillaAlertaSeguridad();
        TipoNotificacionWhatsapp tipo = mapearTipoAlerta(alerta);

        for (Usuario destinatario : destinatarios) {
            if (destinatario.getTelefono() == null || destinatario.getTelefono().isBlank()) {
                continue;
            }
            List<String> parametros = List.of(
                    destinatario.getNombres(),
                    alerta.getDescripcion(),
                    alerta.getFechaHora().format(FORMATO_HORA)
            );
            enviarPlantilla(
                    destinatario.getTelefono(),
                    destinatario.getNombres() + " " + destinatario.getApellidos(),
                    tipo,
                    plantilla,
                    parametros,
                    alerta,
                    null
            );
        }
    }

    /**
     * Notifica al residente dueño del vehículo cuando este ingresa o
     * sale del pueblo. No hace nada si el vehículo no tiene residente
     * asociado (visitante) o el residente no tiene teléfono registrado
     * — se registra como OMITIDO para dejar rastro del motivo.
     */
    @Async("whatsappTaskExecutor")
    public void notificarMovimientoVehiculo(RegistroAcceso registro) {
        Vehiculo vehiculo = registro.getVehiculo();
        if (vehiculo == null || vehiculo.getResidente() == null) {
            return; // Visitante o placa desconocida: no hay a quién notificar.
        }

        Residente residente = vehiculo.getResidente();
        String telefono = residente.getTelefonoContacto();

        TipoNotificacionWhatsapp tipo = registro.getTipoMovimiento() == TipoMovimiento.ENTRADA
                ? TipoNotificacionWhatsapp.INGRESO_VEHICULO
                : TipoNotificacionWhatsapp.SALIDA_VEHICULO;

        String accion = registro.getTipoMovimiento() == TipoMovimiento.ENTRADA ? "ingresó a" : "salió de";

        if (telefono == null || telefono.isBlank()) {
            registrarOmitido(tipo, "N/A", residente.getNombresCompletos(),
                    "Residente sin teléfono de contacto registrado", null, registro);
            return;
        }

        List<String> parametros = List.of(
                residente.getNombresCompletos(),
                registro.getPlaca(),
                accion,
                registro.getFechaHora().format(FORMATO_HORA)
        );

        enviarPlantilla(
                telefono,
                residente.getNombresCompletos(),
                tipo,
                whatsappProperties.getPlantillaMovimientoVehiculo(),
                parametros,
                null,
                registro
        );
    }

    // ===================== NÚCLEO DE ENVÍO =====================

    @Transactional
    protected void enviarPlantilla(
            String telefono,
            String nombreDestino,
            TipoNotificacionWhatsapp tipo,
            String nombrePlantilla,
            List<String> parametros,
            AlertaSeguridad alerta,
            RegistroAcceso registro
    ) {
        NotificacionWhatsapp notificacion = NotificacionWhatsapp.builder()
                .tipo(tipo)
                .telefonoDestino(telefono)
                .nombreDestino(nombreDestino)
                .mensajeResumen(String.join(" | ", parametros))
                .estado(EstadoNotificacionWhatsapp.PENDIENTE)
                .alertaSeguridad(alerta)
                .registroAcceso(registro)
                .build();

        if (!whatsappProperties.isEnabled()) {
            notificacion.setEstado(EstadoNotificacionWhatsapp.OMITIDO);
            notificacion.setErrorDetalle("Integración WhatsApp deshabilitada (app.whatsapp.enabled=false)");
            notificacionRepository.save(notificacion);
            log.info("[WhatsApp OMITIDO] {} -> {}: {}", tipo, telefono, notificacion.getMensajeResumen());
            return;
        }

        try {
            WhatsappTemplateRequest payload = WhatsappTemplateRequest.de(
                    telefono, nombrePlantilla, whatsappProperties.getIdiomaPlantilla(), parametros
            );

            WhatsappApiResponse respuesta = whatsappRestClient.post()
                    .uri("/messages")
                    .body(payload)
                    .retrieve()
                    .body(WhatsappApiResponse.class);

            String messageId = (respuesta != null && !respuesta.messages().isEmpty())
                    ? respuesta.messages().get(0).id()
                    : null;

            notificacion.setEstado(EstadoNotificacionWhatsapp.ENVIADO);
            notificacion.setWhatsappMessageId(messageId);
            notificacion.setFechaEnvio(LocalDateTime.now());
            log.info("WhatsApp enviado a {} ({}), messageId={}", telefono, tipo, messageId);

        } catch (RestClientResponseException ex) {
            notificacion.setEstado(EstadoNotificacionWhatsapp.FALLIDO);
            notificacion.setErrorDetalle(truncar(ex.getResponseBodyAsString(), 500));
            log.error("Meta rechazó el envío a {} ({}): {}", telefono, tipo, ex.getResponseBodyAsString());
        } catch (Exception ex) {
            notificacion.setEstado(EstadoNotificacionWhatsapp.FALLIDO);
            notificacion.setErrorDetalle(truncar(ex.getMessage(), 500));
            log.error("Error de conexión al enviar WhatsApp a {} ({})", telefono, tipo, ex);
        }

        notificacionRepository.save(notificacion);
    }

    private void registrarOmitido(TipoNotificacionWhatsapp tipo, String telefono, String nombre,
                                    String motivo, AlertaSeguridad alerta, RegistroAcceso registro) {
        NotificacionWhatsapp notificacion = NotificacionWhatsapp.builder()
                .tipo(tipo)
                .telefonoDestino(telefono)
                .nombreDestino(nombre)
                .mensajeResumen(motivo)
                .estado(EstadoNotificacionWhatsapp.OMITIDO)
                .errorDetalle(motivo)
                .alertaSeguridad(alerta)
                .registroAcceso(registro)
                .build();
        notificacionRepository.save(notificacion);
    }

    private TipoNotificacionWhatsapp mapearTipoAlerta(AlertaSeguridad alerta) {
        return switch (alerta.getTipo()) {
            case VEHICULO_LISTA_NEGRA -> TipoNotificacionWhatsapp.ALERTA_VEHICULO_LISTA_NEGRA;
            default -> TipoNotificacionWhatsapp.ALERTA_PLACA_NO_REGISTRADA;
        };
    }

    private String truncar(String texto, int max) {
        if (texto == null) return null;
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}