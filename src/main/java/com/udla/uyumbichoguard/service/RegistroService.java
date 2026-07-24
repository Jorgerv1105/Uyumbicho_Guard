package com.udla.uyumbichoguard.service;

import com.udla.uyumbichoguard.dto.request.*;
import com.udla.uyumbichoguard.dto.response.*;
import com.udla.uyumbichoguard.exception.RecursoDuplicadoException;
import com.udla.uyumbichoguard.exception.RecursoNoEncontradoException;
import com.udla.uyumbichoguard.model.*;
import com.udla.uyumbichoguard.model.enums.TipoMovimiento;
import com.udla.uyumbichoguard.provider.VehiculoProvider;
import com.udla.uyumbichoguard.provider.dto.InfoVehiculoExterna;
import com.udla.uyumbichoguard.provider.exception.VehiculoProviderException;
import com.udla.uyumbichoguard.repository.ResidenteRepository;
import com.udla.uyumbichoguard.repository.RegistroAccesoRepository;
import com.udla.uyumbichoguard.repository.VehiculoRepository;
import com.udla.uyumbichoguard.security.AutenticacionUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udla.uyumbichoguard.notificacion.NotificacionWhatsappService;

import java.util.List;
import java.util.Optional;

/**
 * Núcleo funcional de UyumbichoGuard: gestión de residentes, vehículos
 * y el registro de entrada/salida en la garita.
 *
 * Depende de VehiculoProvider (Parte 4, patrón Adapter) para enriquecer
 * datos de vehículos nuevos consultando la fuente externa (Mock en
 * desarrollo, SRI/ANT en producción) sin que este service necesite
 * saber cuál de las dos implementaciones está activa — eso lo resuelve
 * Spring por @Profile al momento de inyectar la dependencia.
 */
@Service
@RequiredArgsConstructor
public class RegistroService {

    private static final Logger log = LoggerFactory.getLogger(RegistroService.class);

    private final ResidenteRepository residenteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final RegistroAccesoRepository registroAccesoRepository;
    private final VehiculoProvider vehiculoProvider;
    private final SeguridadService seguridadService;
    private final NotificacionWhatsappService notificacionWhatsappService;

    // ===================== RESIDENTES =====================

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','VIGILANTE')")
    @Transactional
    public ResidenteResponse crearResidente(CrearResidenteRequest request) {
        if (residenteRepository.existsByCedula(request.cedula())) {
            throw new RecursoDuplicadoException("Ya existe un residente registrado con esa cédula");
        }

        Residente residente = Residente.builder()
                .nombresCompletos(request.nombresCompletos())
                .cedula(request.cedula())
                .telefonoContacto(request.telefonoContacto())
                .manzana(request.manzana())
                .numeroCasa(request.numeroCasa())
                .direccionReferencia(request.direccionReferencia())
                .build();

        return ResidenteResponse.desde(residenteRepository.save(residente));
    }

    @Transactional(readOnly = true)
    public List<ResidenteResponse> listarResidentes() {
        // Sin vehículos en el listado general para evitar N+1 al
        // cargar cientos de residentes; el detalle completo se pide
        // individualmente con obtenerResidentePorId.
        return residenteRepository.findAll().stream()
                .map(ResidenteResponse::desdeSinVehiculos)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResidenteResponse obtenerResidentePorId(Long id) {
        return ResidenteResponse.desde(buscarResidenteOFallar(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional
    public ResidenteResponse actualizarResidente(Long id, ActualizarResidenteRequest request) {
        Residente residente = buscarResidenteOFallar(id);

        if (request.nombresCompletos() != null) residente.setNombresCompletos(request.nombresCompletos());
        if (request.telefonoContacto() != null) residente.setTelefonoContacto(request.telefonoContacto());
        if (request.manzana() != null) residente.setManzana(request.manzana());
        if (request.numeroCasa() != null) residente.setNumeroCasa(request.numeroCasa());
        if (request.direccionReferencia() != null) residente.setDireccionReferencia(request.direccionReferencia());

        return ResidenteResponse.desde(residenteRepository.save(residente));
    }

    // ===================== VEHÍCULOS =====================

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','VIGILANTE')")
    @Transactional
    public VehiculoResponse crearVehiculo(CrearVehiculoRequest request) {
        String placa = normalizarPlaca(request.placa());

        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new RecursoDuplicadoException("Ya existe un vehículo registrado con esa placa");
        }

        Residente residente = null;
        if (request.residenteId() != null) {
            residente = buscarResidenteOFallar(request.residenteId());
        }

        Vehiculo.VehiculoBuilder builder = Vehiculo.builder()
                .placa(placa)
                .marca(request.marca())
                .modelo(request.modelo())
                .color(request.color())
                .tipo(request.tipo())
                .anioFabricacion(request.anioFabricacion())
                .residente(residente)
                .esVisitante(request.esVisitante())
                .activo(true);

        // Enriquecimiento opcional vía el Adapter (Parte 4): si el
        // vigilante no llenó marca/modelo, intentamos completarlos
        // desde el proveedor externo. Un fallo del proveedor NUNCA
        // bloquea el registro del vehículo (degradación con gracia) —
        // la garita debe seguir funcionando aunque el SRI esté caído.
        if (request.marca() == null || request.modelo() == null) {
            enriquecerConProveedorExterno(placa, builder);
        }

        Vehiculo guardado = vehiculoRepository.save(builder.build());
        return VehiculoResponse.desde(guardado);
    }

    private void enriquecerConProveedorExterno(String placa, Vehiculo.VehiculoBuilder builder) {
        try {
            Optional<InfoVehiculoExterna> info = vehiculoProvider.consultarPorPlaca(placa);
            info.ifPresent(datos -> {
                if (datos.marca() != null) builder.marca(datos.marca());
                if (datos.modelo() != null) builder.modelo(datos.modelo());
                if (datos.color() != null) builder.color(datos.color());
            });
        } catch (VehiculoProviderException ex) {
            log.warn("No se pudo consultar el proveedor vehicular ({}) para la placa {}: {}",
                    vehiculoProvider.nombreProveedor(), placa, ex.getMessage());
            // Se continúa sin los datos enriquecidos; no es un error fatal.
        }
    }

    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculos() {
        return vehiculoRepository.findByActivoTrue().stream().map(VehiculoResponse::desde).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','VIGILANTE')")
    @Transactional
    public VehiculoResponse actualizarVehiculo(Long id, ActualizarVehiculoRequest request) {
        Vehiculo vehiculo = buscarVehiculoOFallar(id);

        if (request.marca() != null) vehiculo.setMarca(request.marca());
        if (request.modelo() != null) vehiculo.setModelo(request.modelo());
        if (request.color() != null) vehiculo.setColor(request.color());
        if (request.tipo() != null) vehiculo.setTipo(request.tipo());
        if (request.anioFabricacion() != null) vehiculo.setAnioFabricacion(request.anioFabricacion());

        return VehiculoResponse.desde(vehiculoRepository.save(vehiculo));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional
    public void desactivarVehiculo(Long id) {
        Vehiculo vehiculo = buscarVehiculoOFallar(id);
        // Soft delete: preserva el historial de RegistroAcceso asociado,
        // que no debe perderse aunque el vehículo ya no esté activo.
        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
    }

    // ===================== REGISTRO DE ACCESO (ENTRADA/SALIDA) =====================

    /**
     * Núcleo funcional del sistema: registra el ingreso o salida de un
     * vehículo en la garita. Flujo:
     * 1. Normaliza la placa y busca si corresponde a un vehículo conocido.
     * 2. Persiste el RegistroAcceso con el vigilante autenticado como autor.
     * 3. Si es una ENTRADA: verifica lista negra (alerta CRÍTICA) o
     *    ausencia de registro previo (alerta MEDIA de placa no
     *    registrada). El registro NUNCA se bloquea por esto — el
     *    sistema informa, el vigilante decide, siguiendo el principio
     *    de que la garita debe operar incluso ante datos incompletos.
     */
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','VIGILANTE')")
    @Transactional
    public RegistroAccesoResponse registrarAcceso(RegistrarAccesoRequest request) {
        String placa = normalizarPlaca(request.placa());
        Usuario vigilanteActual = AutenticacionUtils.usuarioAutenticado();

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(placa).orElse(null);

        RegistroAcceso registro = RegistroAcceso.builder()
                .placa(placa)
                .vehiculo(vehiculo)
                .tipoMovimiento(request.tipoMovimiento())
                .vigilante(vigilanteActual)
                .fotoPlacaUrl(request.fotoPlacaUrl())
                .origen(request.origen())
                .confianzaOcr(request.confianzaOcr())
                .observaciones(request.observaciones())
                .build();

        RegistroAcceso guardado = registroAccesoRepository.save(registro);

        // Alertas generadas DESPUÉS de guardar, porque AlertaSeguridad
        // referencia el RegistroAcceso ya persistido (necesita su ID).
        if (request.tipoMovimiento() == TipoMovimiento.ENTRADA) {
            if (seguridadService.estaEnListaNegra(placa)) {
                seguridadService.generarAlertaVehiculoListaNegra(guardado);
            } else if (vehiculo == null) {
                seguridadService.generarAlertaPlacaNoRegistrada(guardado);
            }
        }

        notificacionWhatsappService.notificarMovimientoVehiculo(guardado); // <-- NUEVO

        return RegistroAccesoResponse.desde(guardado);
    }

    @Transactional(readOnly = true)
    public List<RegistroAccesoResponse> historialPorPlaca(String placa) {
        return registroAccesoRepository.findByPlacaOrderByFechaHoraDesc(normalizarPlaca(placa)).stream()
                .map(RegistroAccesoResponse::desde)
                .toList();
    }

    /**
     * Vehículos actualmente DENTRO del pueblo (dashboard, Parte 10).
     * Se apoya en la query de RegistroAccesoRepository (Parte 1) que
     * determina el último movimiento por placa a nivel de base de datos.
     */
    @Transactional(readOnly = true)
    public List<VehiculoActivoResponse> listarVehiculosActivos() {
        return registroAccesoRepository.findVehiculosActualmenteDentro().stream()
                .map(this::mapearAVehiculoActivo)
                .toList();
    }

    private VehiculoActivoResponse mapearAVehiculoActivo(RegistroAcceso registro) {
        Vehiculo vehiculo = registro.getVehiculo();
        Residente residente = vehiculo != null ? vehiculo.getResidente() : null;

        return new VehiculoActivoResponse(
                registro.getPlaca(),
                residente != null ? residente.getNombresCompletos() : null,
                residente != null ? residente.getManzana() : null,
                residente != null ? residente.getNumeroCasa() : null,
                registro.getFechaHora(),
                vehiculo != null ? vehiculo.getEsVisitante() : true
        );
    }

    // ===================== HELPERS =====================

    private Residente buscarResidenteOFallar(Long id) {
        return residenteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Residente no encontrado con id " + id));
    }

    private Vehiculo buscarVehiculoOFallar(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id " + id));
    }

    private String normalizarPlaca(String placa) {
        return placa.trim().toUpperCase().replace("-", "");
    }
}