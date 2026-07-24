package com.udla.uyumbichoguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal de arranque de UyumbichoGuard.
 *
 * @EnableJpaAuditing activa el llenado automático de createdAt/updatedAt
 * definido en AuditoriaBase (Parte 1). Se completará en la Parte 7 con
 * la configuración de scheduling (para expirar bloqueos, etc.) si aplica.
 */
@SpringBootApplication
@EnableJpaAuditing
public class UyumbichoGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(UyumbichoGuardApplication.class, args);
    }
}