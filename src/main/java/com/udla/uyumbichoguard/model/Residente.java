package com.udla.uyumbichoguard.model;

import com.udla.uyumbichoguard.model.enums.EstadoResidente;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de domicilio de un residente de Uyumbicho.
 *
 * La relación con Usuario es OPCIONAL (usuario_id nullable + único):
 * un vigilante o supervisor puede registrar un residente y sus vehículos
 * sin que el residente tenga todavía acceso al portal web. El ADMIN
 * vincula el Usuario después (rol RESIDENTE), sin perder el historial
 * de vehículos ya asociado a este Residente.
 */
@Entity
@Table(name = "residentes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Residente extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación 1:1 opcional. unique=true evita que un mismo Usuario
     * quede vinculado a dos residentes distintos.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nombresCompletos;

    @Column(nullable = false, length = 10)
    private String cedula;

    @Column(length = 15)
    private String telefonoContacto;

    @Column(nullable = false, length = 20)
    private String manzana;

    @Column(name = "numero_casa", nullable = false, length = 20)
    private String numeroCasa;

    @Column(name = "direccion_referencia", length = 255)
    private String direccionReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoResidente estado = EstadoResidente.ACTIVO;

    @OneToMany(mappedBy = "residente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehiculo> vehiculos = new ArrayList<>();
}