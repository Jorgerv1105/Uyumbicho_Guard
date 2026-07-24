package com.udla.uyumbichoguard.security;

import com.udla.uyumbichoguard.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre Spring Security y nuestra tabla de usuarios.
 *
 * Usuario ya implementa UserDetails directamente (Parte 1), así que
 * este servicio solo necesita resolver el email a la entidad.
 *
 * Decisión de seguridad: el mensaje de excepción es genérico
 * ("credenciales inválidas") tanto si el usuario no existe como si
 * existe pero está deshabilitado — se evalúa en AuthenticationProvider,
 * no aquí, para no filtrar por timing/mensaje si un email está
 * registrado o no (mitiga enumeración de usuarios).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}