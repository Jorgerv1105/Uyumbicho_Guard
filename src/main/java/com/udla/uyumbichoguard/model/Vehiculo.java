package com.udla.uyumbichoguard.model;

import com.udla.uyumbichoguard.model.enums.TipoVehiculo;
import jakarta.persistence.*;
import lombok.*;

/**
 * Vehículo registrado en el sistema.
 *
 * `placa` tiene índice único: es el campo por el que se consulta en cada
 * ingreso/salida (posiblemente cientos de veces al día vía OCR), y la
 * unicidad evita registrar el mismo vehículo dos veces con residentes
 * distintos por error.
 *
 * `residente` es nullable a propósito: permite registrar vehículos de
 * visitantes (esVisitante=true) que no pertenecen a ningún residente,
 * útil para llevar bitácora de RegistroAcceso incluso de terceros.
 */
@Entity
@Table(name = "vehiculos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehiculos_placa", columnNames = "placa")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Placa normalizada en mayúsculas sin guiones (ej: PBX1234) para
     * simplificar comparación exacta contra el resultado del OCR.
     * El formato de presentación (con guión) se maneja en el frontend.
     */
    @Column(nullable = false, length = 10)
    private String placa;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVehiculo tipo;

    @Column(name = "anio_fabricacion")
    private Integer anioFabricacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residente_id")
    private Residente residente;

    @Column(name = "es_visitante", nullable = false)
    @Builder.Default
    private Boolean esVisitante = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}