package com.udla.uyumbichoguard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que se ejecuta UNA vez por request (OncePerRequestFilter).
 *
 * Flujo:
 * 1. Extrae el header Authorization: Bearer <token>.
 * 2. Si no hay token o no empieza con "Bearer ", continúa la cadena
 *    sin autenticar (rutas públicas como /api/auth/login lo permiten;
 *    rutas protegidas serán rechazadas después por SecurityConfig).
 * 3. Si hay token, lo valida y puebla el SecurityContext para que el
 *    resto del pipeline (controllers, @PreAuthorize) sepa quién es el
 *    usuario autenticado.
 *
 * Decisión de seguridad: cualquier excepción de parseo/validación del
 * token (firma inválida, expirado, malformado) se traga silenciosamente
 * aquí y simplemente NO se autentica al usuario — la request seguirá
 * su curso como anónima y será rechazada más adelante por
 * SecurityConfig si la ruta requiere autenticación. Esto evita filtrar
 * detalles del error de JWT al cliente en este punto (se maneja de
 * forma uniforme en JwtAuthenticationEntryPoint).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTORIZACION = "Authorization";
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HEADER_AUTORIZACION);

        if (authHeader == null || !authHeader.startsWith(PREFIJO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(PREFIJO_BEARER.length());

        try {
            final String email = jwtService.extractUsername(jwt);

            // Solo autenticamos si aún no hay una autenticación en el
            // contexto (evita reprocesar en forwards internos).
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // credentials null: ya no necesitamos la password una vez autenticado
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Token inválido/expirado/malformado -> se ignora, la
            // request continúa sin autenticar (ver javadoc de la clase).
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}