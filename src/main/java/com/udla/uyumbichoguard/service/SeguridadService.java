package com.udla.uyumbichoguard.service;

import com.udla.uyumbichoguard.dto.request.AgregarListaNegraRequest;
import com.udla.uyumbichoguard.dto.request.AtenderAlertaRequest;
import com.udla.uyumbichoguard.dto.response.AlertaSeguridadResponse;
import com.udla.uyumbichoguard.dto.response.ListaNegraResponse;
import com.udla.uyumbichoguard.exception.RecursoNoEncontradoException;
import com.udla.uyumbichoguard.model.AlertaSeguridad;
import com.udla.uyumbichoguard.model.ListaNegra;
import com.udla.uyumbichoguard.model.RegistroAcceso;
import com.udla.uyumbichoguard.model.Usuario;
import com.udla.uyumbichoguard.model.enums.NivelAlerta;
import com.udla.uyumbichoguard.model.enums.TipoAlerta;
import com.udla.uyumbichoguard.repository.AlertaSeguridadRepository;
import com.udla.uyumbichoguard.repository.ListaNegraRepository;
import com.udla.uyumbichoguard.security.AutenticacionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udla.uyumbichoguard.notificacion.NotificacionWhatsappService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestiona la lista negra de placas y las alertas de seguridad.
 *
 * Es consumido por RegistroService (registro de entrada/salida) y por
 * AuthService (intentos fallidos de login) para generar alertas
 * automáticamente — por eso los métodos "generarAlerta*" son públicos
 * pero SIN @PreAuthorize: se ejecutan como efecto secundario dentro
 * del contexto de seguridad de quien ya está autenticado haciendo la
 * operación principal (el vigilante registrando un acceso, o el propio
 * intento de login), no como una acción directa del usuario final.
 */
@Service
@RequiredArgsConstructor
public class SeguridadService {

    private final ListaNegraRepository listaNegraRepository;
    private final AlertaSeguridadRepository alertaSeguridadRepository;
    private final NotificacionWhatsappService notificacionWhatsappService;

    // ===================== LISTA NEGRA =====================

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional
    public ListaNegraResponse agregarListaNegra(AgregarListaNegraRequest request) {
        Usuario actual = AutenticacionUtils.usuarioAutenticado();

        ListaNegra registro = ListaNegra.builder()
                .placa(normalizarPlaca(request.placa()))
                .motivo(request.motivo())
                .registradoPor(actual)
                .fechaExpiracion(request.fechaExpiracion())
                .activo(true)
                .build();

        return ListaNegraResponse.desde(listaNegraRepository.save(registro));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional
    public void quitarDeListaNegra(Long id) {
        ListaNegra registro = listaNegraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de lista negra no encontrado con id " + id));
        // Soft delete: preserva el historial completo para auditoría de seguridad.
        registro.setActivo(false);
        listaNegraRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<ListaNegraResponse> listarListaNegraActiva() {
        return listaNegraRepository.findByActivoTrue().stream()
                .map(ListaNegraResponse::desde)
                .toList();
    }

    /**
     * Usado por RegistroService para decidir si un vehículo que
     * intenta ingresar está bloqueado. La expiración se evalúa
     * automáticamente en la query (ver ListaNegraRepository, Parte 1).
     */
    @Transactional(readOnly = true)
    public boolean estaEnListaNegra(String placa) {
        return listaNegraRepository.findBloqueoActivoPorPlaca(normalizarPlaca(placa)).isPresent();
    }

    // ===================== ALERTAS DE SEGURIDAD =====================

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<AlertaSeguridadResponse> listarAlertasPendientes() {
        return alertaSeguridadRepository.findByAtendidaFalseOrderByFechaHoraDesc().stream()
                .map(AlertaSeguridadResponse::desde)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional
    public AlertaSeguridadResponse atenderAlerta(Long id, AtenderAlertaRequest request) {
        AlertaSeguridad alerta = alertaSeguridadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada con id " + id));

        Usuario actual = AutenticacionUtils.usuarioAutenticado();
        alerta.setAtendida(true);
        alerta.setAtendidaPor(actual);
        alerta.setFechaAtencion(LocalDateTime.now());
        if (request != null && request.observacion() != null && !request.observacion().isBlank()) {
            alerta.setDescripcion(alerta.getDescripcion() + " | Observación: " + request.observacion());
        }

        return AlertaSeguridadResponse.desde(alertaSeguridadRepository.save(alerta));
    }

    /** Generada por RegistroService cuando un vehículo en lista negra intenta ingresar/salir. */
    @Transactional
    public void generarAlertaVehiculoListaNegra(RegistroAcceso registro) {
        AlertaSeguridad alerta = AlertaSeguridad.builder()
                .tipo(TipoAlerta.VEHICULO_LISTA_NEGRA)
                .nivel(NivelAlerta.CRITICA)
                .descripcion("Vehículo con placa %s en lista negra intentó %s"
                        .formatted(registro.getPlaca(), registro.getTipoMovimiento()))
                .registroAcceso(registro)
                .atendida(false)
                .build();
        AlertaSeguridad guardada = alertaSeguridadRepository.save(alerta);
        notificacionWhatsappService.notificarAlertaSeguridad(guardada); // <-- NUEVO
    }

    /** Generada por RegistroService cuando la placa no corresponde a ningún vehículo conocido. */
    @Transactional
    public void generarAlertaPlacaNoRegistrada(RegistroAcceso registro) {
        AlertaSeguridad alerta = AlertaSeguridad.builder()
                .tipo(TipoAlerta.PLACA_NO_REGISTRADA)
                .nivel(NivelAlerta.MEDIA)
                .descripcion("Placa %s no está registrada en el sistema (movimiento: %s)"
                        .formatted(registro.getPlaca(), registro.getTipoMovimiento()))
                .registroAcceso(registro)
                .atendida(false)
                .build();
        AlertaSeguridad guardada = alertaSeguridadRepository.save(alerta);
        notificacionWhatsappService.notificarAlertaSeguridad(guardada); // <-- NUEVO
    }

    /** Generada por AuthService cuando una cuenta se bloquea por múltiples intentos fallidos. */
    @Transactional
    public void generarAlertaIntentosFallidosLogin(String emailAfectado) {
        AlertaSeguridad alerta = AlertaSeguridad.builder()
                .tipo(TipoAlerta.MULTIPLES_INTENTOS_FALLIDOS_LOGIN)
                .nivel(NivelAlerta.ALTA)
                .descripcion("Cuenta %s bloqueada temporalmente por múltiples intentos fallidos de inicio de sesión"
                        .formatted(emailAfectado))
                .atendida(false)
                .build();
        alertaSeguridadRepository.save(alerta);
    }

    private String normalizarPlaca(String placa) {
        return placa.trim().toUpperCase().replace("-", "");
    }
}