package com.udla.uyumbichoguard.repository;

import com.udla.uyumbichoguard.model.NotificacionWhatsapp;
import com.udla.uyumbichoguard.model.enums.EstadoNotificacionWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionWhatsappRepository extends JpaRepository<NotificacionWhatsapp, Long> {

    List<NotificacionWhatsapp> findByEstadoOrderByCreatedAtDesc(EstadoNotificacionWhatsapp estado);

    List<NotificacionWhatsapp> findTop100ByOrderByCreatedAtDesc();
}