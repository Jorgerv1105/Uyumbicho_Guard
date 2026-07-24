package com.udla.uyumbichoguard.model;

import com.udla.uyumbichoguard.model.enums.EstadoNotificacionWhatsapp;
import com.udla.uyumbichoguard.model.enums.TipoNotificacionWhatsapp;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Auditoría de cada intento de notificación por WhatsApp — importante
 * para la defensa de tesis (trazabilidad: qué se envió, a quién, y si
 * Meta confirmó la entrega) y para depurar fallos de integración en
 * producción sin depender de los logs del servidor únicamente.
 *
 * El teléfono se guarda DENORMALIZADO (copiado tal cual al momento del
 * envío) a propósito: si el residente cambia su número después, el
 * historial de auditoría debe reflejar a qué número se envió
 * REALMENTE en ese momento, no el número actual.
 */
@Entity
@Table(name = "notificaciones_whatsapp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionWhatsapp extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacionWhatsapp tipo;

    @Column(name = "telefono_destino", nullable = false, length = 20)
    private String telefonoDestino;

    @Column(name = "nombre_destino", length = 150)
    private String nombreDestino;

    /** Resumen legible del contenido enviado, útil para revisar en el
     * panel de administración sin tener que ir a Meta Business Manager. */
    @Column(name = "mensaje_resumen", length = 500)
    private String mensajeResumen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoNotificacionWhatsapp estado = EstadoNotificacionWhatsapp.PENDIENTE;

    /** ID del mensaje devuelto por Meta cuando el envío es exitoso;
     * permite correlacionar con webhooks de estado de entrega futuros. */
    @Column(name = "whatsapp_message_id", length = 100)
    private String whatsappMessageId;

    @Column(name = "error_detalle", length = 500)
    private String errorDetalle;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_seguridad_id")
    private AlertaSeguridad alertaSeguridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_acceso_id")
    private RegistroAcceso registroAcceso;
}