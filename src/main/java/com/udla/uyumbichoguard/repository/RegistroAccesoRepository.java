package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {

    List<RegistroAcceso> findByPlacaOrderByFechaHoraDesc(String placa);

    List<RegistroAcceso> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);

    /**
     * Trae el último movimiento registrado para una placa: permite saber
     * si el vehículo está actualmente DENTRO (último movimiento=ENTRADA)
     * o FUERA (último movimiento=SALIDA), clave para el dashboard de
     * "vehículos activos" (Parte 10).
     */
    @Query("""
            SELECT r FROM RegistroAcceso r
            WHERE r.placa = :placa
            ORDER BY r.fechaHora DESC
            LIMIT 1
            """)
    Optional<RegistroAcceso> findUltimoMovimientoPorPlaca(@Param("placa") String placa);

    @Query("""
            SELECT r FROM RegistroAcceso r
            WHERE r.tipoMovimiento = com.udla.uyumbichoguard.model.enums.TipoMovimiento.ENTRADA
            AND NOT EXISTS (
                SELECT 1 FROM RegistroAcceso s
                WHERE s.placa = r.placa
                AND s.fechaHora > r.fechaHora
            )
            """)
    List<RegistroAcceso> findVehiculosActualmenteDentro();
}