package com.udla.uyumbichoguard.model;

import com.udla.uyumbichoguard.model.enums.EstadoUsuario;
import com.udla.uyumbichoguard.model.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Usuario del sistema (cuenta de autenticación).
 *
 * Decisiones de seguridad:
 * - El campo `password` SIEMPRE almacena el hash BCrypt (cost 12), nunca
 *   texto plano. El hashing se hace en el service, no aquí.
 * - `cedula` y `email` son únicos: evita suplantación y duplicados.
 * - `intentosFallidos` + `fechaBloqueoHasta` implementan bloqueo temporal
 *   por fuerza bruta (rate limiting a nivel de cuenta, complementario al
 *   rate limiting de IP con Bucket4j de la Parte 2).
 * - Implementa UserDetails directamente para que JwtService y el filtro
 *   de autenticación (Parte 2) puedan usar esta entidad sin DTOs extra.
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_cedula", columnNames = "cedula"),
        @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends AuditoriaBase implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Hash BCrypt de la contraseña (cost factor 12). Nunca exponer en DTOs
     * de salida ni loguear este campo.
     */
    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    /**
     * Contador de intentos fallidos consecutivos de login.
     * Se resetea a 0 en un login exitoso.
     */
    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private Integer intentosFallidos = 0;

    /**
     * Si no es null y es futura, la cuenta está bloqueada temporalmente
     * (bloqueo automático por fuerza bruta, distinto de estado=BLOQUEADO
     * que es un bloqueo manual/permanente del ADMIN).
     */
    @Column(name = "fecha_bloqueo_hasta")
    private LocalDateTime fechaBloqueoHasta;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    // ---------- Implementación de UserDetails (Spring Security) ----------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Prefijo ROLE_ requerido por hasRole() de Spring Security.
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        // El login se hace con email o cédula, definido en AuthService (Parte 5).
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        boolean bloqueoTemporalActivo = fechaBloqueoHasta != null
                && fechaBloqueoHasta.isAfter(LocalDateTime.now());
        return estado != EstadoUsuario.BLOQUEADO && !bloqueoTemporalActivo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return estado == EstadoUsuario.ACTIVO;
    }
}