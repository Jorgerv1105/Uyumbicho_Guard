package com.udla.uyumbichoguard.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Configuración central de Spring Security 7.
 *
 * Decisiones de seguridad clave:
 * - Sesiones STATELESS: la API nunca crea HttpSession; toda la
 *   identidad viaja en el JWT de cada request. Elimina CSRF como
 *   vector de ataque relevante (por eso se deshabilita csrf), ya que
 *   CSRF explota cookies de sesión enviadas automáticamente por el
 *   navegador, algo que no ocurre con el header Authorization.
 * - BCrypt con cost factor 12: balance entre seguridad (resistencia a
 *   fuerza bruta offline) y performance aceptable en el hardware
 *   típico de despliegue. 12 es el estándar recomendado en 2026 para
 *   contraseñas de usuarios finales (10 es el mínimo aceptable, 14+
 *   es excesivo para esta escala de proyecto).
 * - Autorización por rol declarada aquí a nivel de ruta (grueso) +
 *   @PreAuthorize a nivel de método en los services/controllers
 *   (fino, Parte 5/6) gracias a @EnableMethodSecurity.
 * - CORS explícito y restringido a los orígenes del frontend — nunca
 *   se usa "*" en producción porque permitiría que cualquier sitio
 *   web hiciera requests autenticadas a nuestra API.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize/@PostAuthorize en services y controllers
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Cost factor 12 explícito (no dejar el default de la librería,
        // que podría cambiar entre versiones sin que nos demos cuenta).
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider conecta nuestro UserDetailsService con
     * el PasswordEncoder para validar credenciales en el login
     * (usado por AuthService en la Parte 5).
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF innecesario en API stateless con JWT (ver javadoc)
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: login y refresh de token
                        .requestMatchers("/api/auth/**").permitAll()
                        // Health check para Docker/monitoreo (Parte 13)
                        .requestMatchers("/actuator/health").permitAll()
                        // Todo lo demás requiere autenticación; la
                        // autorización fina por rol se hace con
                        // @PreAuthorize en cada controller (Parte 6)
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider())

                // Nuestro filtro de rate limiting corre ANTES que el
                // de autenticación JWT: no tiene sentido validar un
                // token si la IP ya superó el límite de intentos.
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitingFilter.class);

        return http.build();
    }

    /**
     * Configuración CORS. Los orígenes reales del frontend (dominio de
     * producción, localhost:5173 en desarrollo) se definen en
     * application.yml (Parte 7) para no hardcodear valores de entorno
     * en código — aquí se deja un default seguro de solo-desarrollo.
     */
    @Value("${app.cors.allowed-origins}")
    private List<String> corsAllowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsAllowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}