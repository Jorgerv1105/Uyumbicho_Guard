package com.udla.uyumbichoguard.controller;

import com.udla.uyumbichoguard.dto.request.AgregarListaNegraRequest;
import com.udla.uyumbichoguard.dto.request.AtenderAlertaRequest;
import com.udla.uyumbichoguard.dto.response.AlertaSeguridadResponse;
import com.udla.uyumbichoguard.dto.response.ListaNegraResponse;
import com.udla.uyumbichoguard.service.SeguridadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seguridad")
@RequiredArgsConstructor
public class SeguridadController {

    private final SeguridadService seguridadService;

    // ===================== LISTA NEGRA =====================

    @PostMapping("/lista-negra")
    public ResponseEntity<ListaNegraResponse> agregarListaNegra(@Valid @RequestBody AgregarListaNegraRequest request) {
        ListaNegraResponse creado = seguridadService.agregarListaNegra(request);
        return ResponseEntity.created(URI.create("/api/seguridad/lista-negra/" + creado.id())).body(creado);
    }

    @GetMapping("/lista-negra")
    public ResponseEntity<List<ListaNegraResponse>> listarListaNegra() {
        return ResponseEntity.ok(seguridadService.listarListaNegraActiva());
    }

    @DeleteMapping("/lista-negra/{id}")
    public ResponseEntity<Void> quitarDeListaNegra(@PathVariable Long id) {
        seguridadService.quitarDeListaNegra(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== ALERTAS =====================

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaSeguridadResponse>> listarAlertasPendientes() {
        return ResponseEntity.ok(seguridadService.listarAlertasPendientes());
    }

    @PatchMapping("/alertas/{id}/atender")
    public ResponseEntity<AlertaSeguridadResponse> atenderAlerta(
            @PathVariable Long id,
            @RequestBody(required = false) AtenderAlertaRequest request
    ) {
        return ResponseEntity.ok(seguridadService.atenderAlerta(id, request));
    }
}