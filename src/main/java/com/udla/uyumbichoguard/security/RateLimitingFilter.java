package com.udla.uyumbichoguard.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita intentos de login por IP usando el algoritmo de token bucket
 * (Bucket4j). Es una capa DISTINTA y complementaria al bloqueo por
 * cuenta (Usuario.intentosFallidos / fechaBloqueoHasta de la Parte 1):
 *
 * - Este filtro protege contra fuerza bruta DISTRIBUIDA (un atacante
 *   probando muchos emails distintos desde la misma IP).
 * - El bloqueo de cuenta protege un usuario específico sin importar
 *   desde qué IP vengan los intentos.
 * Usar ambos juntos es la práctica recomendada (defense in depth).
 *
 * Límite: 5 intentos por minuto por IP hacia /api/auth/login.
 * Se aplica solo a esa ruta; el resto de endpoints no pasa por este
 * filtro (ver shouldNotFilter).
 *
 * NOTA para producción: este mapa en memoria funciona para una sola
 * instancia del backend. Si se escala horizontalmente (más de una
 * instancia detrás de un balanceador), se debería migrar a un backend
 * distribuido de Bucket4j (Redis) — fuera del alcance de esta tesis.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RUTA_LOGIN = "/api/auth/login";
    private static final int CAPACIDAD_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = obtenerIpCliente(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::crearBucketParaIp);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"error":"Demasiados intentos de inicio de sesión. Intenta de nuevo en unos minutos."}
                    """);
        }
    }

    private Bucket crearBucketParaIp(String ip) {
        Bandwidth limite = Bandwidth.classic(
                CAPACIDAD_INTENTOS,
                Refill.greedy(CAPACIDAD_INTENTOS, VENTANA)
        );
        return Bucket.builder().addLimit(limite).build();
    }

    /**
     * Considera el header X-Forwarded-For si la app está detrás de un
     * proxy/Nginx (nuestro caso, ver Parte 13), ya que de lo contrario
     * todas las requests parecerían venir de la IP del proxy.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Solo aplicamos el filtro a la ruta de login; todo lo demás pasa
     * de largo sin overhead de rate limiting a nivel de filtro (otros
     * endpoints sensibles pueden protegerse en Parte 6 a nivel de
     * controller si se requiere).
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getServletPath().equals(RUTA_LOGIN);
    }
}