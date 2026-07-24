package com.udla.uyumbichoguard.service;

import com.udla.uyumbichoguard.dto.request.LoginRequest;
import com.udla.uyumbichoguard.dto.request.RefreshTokenRequest;
import com.udla.uyumbichoguard.dto.response.AuthResponse;
import com.udla.uyumbichoguard.dto.response.UsuarioResponse;
import com.udla.uyumbichoguard.exception.CredencialesInvalidasException;
import com.udla.uyumbichoguard.exception.CuentaBloqueadaException;
import com.udla.uyumbichoguard.model.Usuario;
import com.udla.uyumbichoguard.model.enums.EstadoUsuario;
import com.udla.uyumbichoguard.repository.UsuarioRepository;
import com.udla.uyumbichoguard.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Autenticación y renovación de tokens.
 *
 * Decisión de seguridad central: implementamos el bloqueo por
 * intentos fallidos manualmente aquí (en vez de delegar 100% en
 * AuthenticationManager/DaoAuthenticationProvider de la Parte 2)
 * porque necesitamos control fino sobre el contador
 * Usuario.intentosFallidos y su persistencia — algo que el
 * AuthenticationProvider estándar de Spring no maneja por defecto.
 * El DaoAuthenticationProvider configurado en SecurityConfig queda
 * disponible para otros flujos (ej. Basic Auth de testing), pero el
 * login real de la API pasa por este servicio.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Umbral de intentos fallidos antes de bloquear temporalmente la cuenta. */
    private static final int MAX_INTENTOS_FALLIDOS = 5;

    /** Duración del bloqueo temporal tras exceder el umbral. */
    private static final long MINUTOS_BLOQUEO = 15;

    /**
     * Mensaje ÚNICO para email inexistente, password incorrecta, o
     * cualquier combinación — mitiga enumeración de usuarios (un
     * atacante no puede distinguir "el email no existe" de "el email
     * existe pero la clave está mal").
     */
    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SeguridadService seguridadService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.info("Intento de login con email no registrado: {}", request.email());
                    return new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
                });

        verificarBloqueoTemporal(usuario);

        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new CuentaBloqueadaException("Tu cuenta está inactiva. Contacta a un administrador.");
        }
        if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
            throw new CuentaBloqueadaException("Tu cuenta ha sido bloqueada. Contacta a un administrador.");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            registrarIntentoFallido(usuario);
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        // Login exitoso: resetear contador y registrar último acceso.
        usuario.setIntentosFallidos(0);
        usuario.setFechaBloqueoHasta(null);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return construirAuthResponse(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String email;
        try {
            email = jwtService.extractUsername(request.refreshToken());
        } catch (Exception ex) {
            throw new CredencialesInvalidasException("El refresh token es inválido o expiró");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("El refresh token es inválido o expiró"));

        if (!jwtService.isTokenValid(request.refreshToken(), usuario)) {
            throw new CredencialesInvalidasException("El refresh token es inválido o expiró");
        }

        // Solo se emite un nuevo access token; no rotamos el refresh
        // token en cada uso (simplicidad apropiada para el alcance de
        // esta tesis — rotación de refresh tokens queda como mejora
        // futura documentada en el informe).
        String nuevoAccessToken = jwtService.generateAccessToken(usuario);
        return new AuthResponse(nuevoAccessToken, request.refreshToken(), UsuarioResponse.desde(usuario));
    }

    private void verificarBloqueoTemporal(Usuario usuario) {
        if (usuario.getFechaBloqueoHasta() != null && usuario.getFechaBloqueoHasta().isAfter(LocalDateTime.now())) {
            throw new CuentaBloqueadaException(
                    "Cuenta bloqueada temporalmente por múltiples intentos fallidos. Intenta después de las %s"
                            .formatted(usuario.getFechaBloqueoHasta())
            );
        }
    }

    private void registrarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            usuario.setFechaBloqueoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            log.warn("Cuenta {} bloqueada temporalmente tras {} intentos fallidos", usuario.getEmail(), intentos);
            seguridadService.generarAlertaIntentosFallidosLogin(usuario.getEmail());
        }

        usuarioRepository.save(usuario);
    }

    private AuthResponse construirAuthResponse(Usuario usuario) {
        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);
        return new AuthResponse(accessToken, refreshToken, UsuarioResponse.desde(usuario));
    }
}