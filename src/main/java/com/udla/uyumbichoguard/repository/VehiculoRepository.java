package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    List<Vehiculo> findByResidenteId(Long residenteId);

    List<Vehiculo> findByActivoTrue();
}