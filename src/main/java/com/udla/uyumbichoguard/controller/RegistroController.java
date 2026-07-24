package com.udla.uyumbichoguard.controller;

import com.udla.uyumbichoguard.dto.request.*;
import com.udla.uyumbichoguard.dto.response.*;
import com.udla.uyumbichoguard.service.RegistroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Agrupa los tres sub-dominios estrechamente relacionados de
 * RegistroService (Parte 5): residentes, vehículos y el registro de
 * entrada/salida en la garita. Se mantienen bajo un mismo controller
 * porque comparten el mismo service y ciclo de vida de negocio.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    // ===================== RESIDENTES =====================

    @PostMapping("/residentes")
    public ResponseEntity<ResidenteResponse> crearResidente(@Valid @RequestBody CrearResidenteRequest request) {
        ResidenteResponse creado = registroService.crearResidente(request);
        return ResponseEntity.created(URI.create("/api/residentes/" + creado.id())).body(creado);
    }

    @GetMapping("/residentes")
    public ResponseEntity<List<ResidenteResponse>> listarResidentes() {
        return ResponseEntity.ok(registroService.listarResidentes());
    }

    @GetMapping("/residentes/{id}")
    public ResponseEntity<ResidenteResponse> obtenerResidente(@PathVariable Long id) {
        return ResponseEntity.ok(registroService.obtenerResidentePorId(id));
    }

    @PatchMapping("/residentes/{id}")
    public ResponseEntity<ResidenteResponse> actualizarResidente(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarResidenteRequest request
    ) {
        return ResponseEntity.ok(registroService.actualizarResidente(id, request));
    }

    // ===================== VEHÍCULOS =====================

    @PostMapping("/vehiculos")
    public ResponseEntity<VehiculoResponse> crearVehiculo(@Valid @RequestBody CrearVehiculoRequest request) {
        VehiculoResponse creado = registroService.crearVehiculo(request);
        return ResponseEntity.created(URI.create("/api/vehiculos/" + creado.id())).body(creado);
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculos() {
        return ResponseEntity.ok(registroService.listarVehiculos());
    }

    @PatchMapping("/vehiculos/{id}")
    public ResponseEntity<VehiculoResponse> actualizarVehiculo(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarVehiculoRequest request
    ) {
        return ResponseEntity.ok(registroService.actualizarVehiculo(id, request));
    }

    @DeleteMapping("/vehiculos/{id}")
    public ResponseEntity<Void> desactivarVehiculo(@PathVariable Long id) {
        registroService.desactivarVehiculo(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== REGISTRO DE ACCESO (GARITA) =====================

    @PostMapping("/accesos")
    public ResponseEntity<RegistroAccesoResponse> registrarAcceso(@Valid @RequestBody RegistrarAccesoRequest request) {
        RegistroAccesoResponse creado = registroService.registrarAcceso(request);
        return ResponseEntity.status(201).body(creado);
    }

    @GetMapping("/accesos/historial")
    public ResponseEntity<List<RegistroAccesoResponse>> historialPorPlaca(@RequestParam String placa) {
        return ResponseEntity.ok(registroService.historialPorPlaca(placa));
    }

    @GetMapping("/accesos/activos")
    public ResponseEntity<List<VehiculoActivoResponse>> vehiculosActivos() {
        return ResponseEntity.ok(registroService.listarVehiculosActivos());
    }
}