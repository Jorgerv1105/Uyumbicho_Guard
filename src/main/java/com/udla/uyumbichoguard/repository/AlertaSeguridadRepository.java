package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.AlertaSeguridad;
import com.udla.uyumbichoguard.model.enums.NivelAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaSeguridadRepository extends JpaRepository<AlertaSeguridad, Long> {

    List<AlertaSeguridad> findByAtendidaFalseOrderByFechaHoraDesc();

    List<AlertaSeguridad> findByNivelOrderByFechaHoraDesc(NivelAlerta nivel);
}