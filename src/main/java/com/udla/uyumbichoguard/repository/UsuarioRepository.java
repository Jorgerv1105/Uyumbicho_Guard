package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.Usuario;
import com.udla.uyumbichoguard.model.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCedula(String cedula);

    boolean existsByEmail(String email);

    boolean existsByCedula(String cedula);

    List<Usuario> findByRol(RolUsuario rol);
}