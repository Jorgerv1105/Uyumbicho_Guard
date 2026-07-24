package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.Residente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResidenteRepository extends JpaRepository<Residente, Long> {

    Optional<Residente> findByCedula(String cedula);

    Optional<Residente> findByUsuarioId(Long usuarioId);

    boolean existsByCedula(String cedula);
}