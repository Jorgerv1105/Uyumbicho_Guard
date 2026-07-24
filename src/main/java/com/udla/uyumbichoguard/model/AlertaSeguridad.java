package com.udla.uyumbichoguard.model;

import com.udla.uyumbichoguard.model.enums.NivelAlerta;
import com.udla.uyumbichoguard.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evento de seguridad generado por el sistema (ej: intento de ingreso de
 * un vehículo en lista negra, placa no reconocida por el OCR, múltiples
 * intentos fallidos de login). Permite al SUPERVISOR/ADMIN monitorear y
 * dar seguimiento ("atender") cada evento desde el dashboard.
 */
@Entity
@Table(name = "alertas_seguridad")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelAlerta nivel;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    /**
     * Registro de acceso que originó la alerta, si aplica (no todas las
     * alertas vienen de un ingreso, ej: MULTIPLES_INTENTOS_FALLIDOS_LOGIN).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_acceso_id")
    private RegistroAcceso registroAcceso;

    @Column(nullable = false)
    @Builder.Default
    private Boolean atendida = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendida_por_id")
    private Usuario atendidaPor;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;
}