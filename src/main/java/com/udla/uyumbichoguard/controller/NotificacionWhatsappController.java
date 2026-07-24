package com.udla.uyumbichoguard.controller;

import com.udla.uyumbichoguard.dto.response.NotificacionWhatsappResponse;
import com.udla.uyumbichoguard.repository.NotificacionWhatsappRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura, para que ADMIN/SUPERVISOR auditen qué notificaciones
 * de WhatsApp se enviaron, a quién, y si Meta las confirmó — útil en
 * la defensa de tesis para demostrar trazabilidad, y en producción
 * para depurar reclamos de "no me llegó el WhatsApp".
 */
@RestController
@RequestMapping("/api/notificaciones-whatsapp")
@RequiredArgsConstructor
public class NotificacionWhatsappController {

    private final NotificacionWhatsappRepository notificacionWhatsappRepository;

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @GetMapping
    public ResponseEntity<List<NotificacionWhatsappResponse>> listarRecientes() {
        List<NotificacionWhatsappResponse> respuesta = notificacionWhatsappRepository
                .findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(NotificacionWhatsappResponse::desde)
                .toList();
        return ResponseEntity.ok(respuesta);
    }
}