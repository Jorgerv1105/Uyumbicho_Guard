package com.udla.uyumbichoguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal de arranque de UyumbichoGuard.
 *
 * @EnableJpaAuditing activa createdAt/updatedAt automáticos
 * (AuditoriaBase, Parte 1). @EnableAsync para el envío de WhatsApp
 * vive en AsyncConfig (Parte 5B), no aquí, para mantener cada
 * @Configuration enfocada en una sola responsabilidad.
 */
@SpringBootApplication
@EnableJpaAuditing
public class UyumbichoGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(UyumbichoGuardApplication.class, args);
    }
}