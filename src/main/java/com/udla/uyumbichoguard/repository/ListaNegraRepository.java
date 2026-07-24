package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.ListaNegra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaNegraRepository extends JpaRepository<ListaNegra, Long> {

    List<ListaNegra> findByActivoTrue();

    /**
     * Busca si una placa está activa en lista negra y aún no ha vencido.
     * Se valida la expiración en la query para no depender de un job
     * programado que desactive registros vencidos.
     */
    @Query("""
            SELECT l FROM ListaNegra l
            WHERE l.placa = :placa
            AND l.activo = true
            AND (l.fechaExpiracion IS NULL OR l.fechaExpiracion > CURRENT_TIMESTAMP)
            """)
    Optional<ListaNegra> findBloqueoActivoPorPlaca(@Param("placa") String placa);
}