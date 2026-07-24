package com.udla.uyumbichoguard.model;

import com.udla.uyumbichoguard.model.enums.OrigenRegistro;
import com.udla.uyumbichoguard.model.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bitácora de cada ingreso/salida registrado en la garita.
 *
 * `placa` se guarda SIEMPRE como texto plano capturado (por OCR o
 * digitado), incluso si no hay match con un Vehiculo conocido: esto es
 * fundamental para poder auditar placas no reconocidas y para generar
 * la AlertaSeguridad de PLACA_NO_REGISTRADA.
 *
 * `vehiculo` es nullable: si la placa no coincide con ningún vehículo
 * registrado, igual queda el registro de acceso para trazabilidad.
 */
@Entity
@Table(name = "registros_acceso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String placa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    /**
     * Vigilante que registró el movimiento (o que estaba de turno cuando
     * el OCR lo generó automáticamente). Nunca null: todo registro debe
     * quedar atribuido a un usuario autenticado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vigilante_id", nullable = false)
    private Usuario vigilante;

    @Column(name = "foto_placa_url", length = 500)
    private String fotoPlacaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenRegistro origen;

    /**
     * Score de confianza (0-100) devuelto por el microservicio OCR.
     * Null si el origen es MANUAL. Se usa para decidir si el registro
     * automático requiere revisión humana (ej: confianza < 80%).
     */
    @Column(name = "confianza_ocr")
    private Double confianzaOcr;

    @Column(length = 255)
    private String observaciones;
}