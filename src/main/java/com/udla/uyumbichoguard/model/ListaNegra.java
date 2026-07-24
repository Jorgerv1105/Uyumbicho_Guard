package com.udla.uyumbichoguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Placas marcadas como no autorizadas para ingresar al pueblo.
 *
 * Entidad independiente de Vehiculo (ver justificación de diseño):
 * permite registrar en lista negra placas que NUNCA se registraron
 * como Vehiculo (ej: reportadas por la policía comunitaria o por un
 * residente), y mantiene historial aunque se reactive/desactive.
 *
 * `activo` permite "quitar" una placa de la lista negra sin borrar el
 * historial (soft delete), importante para auditoría de seguridad.
 */
@Entity
@Table(name = "lista_negra")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListaNegra extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(nullable = false, length = 255)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    /**
     * Null = bloqueo permanente. Si tiene fecha, el registro se considera
     * vencido automáticamente después de esa fecha (validado en el
     * service, no requiere job).
     */
    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}