package com.udla.uyumbichoguard.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Superclase mapeada para trazabilidad temporal (createdAt / updatedAt).
 * Importante para auditoría de seguridad: permite reconstruir cuándo se
 * creó o modificó cualquier registro sensible (usuarios, vehículos,
 * lista negra, etc.) ante una investigación o reclamo.
 *
 * Requiere @EnableJpaAuditing en la clase principal (se agrega en Parte 7).
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditoriaBase {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}