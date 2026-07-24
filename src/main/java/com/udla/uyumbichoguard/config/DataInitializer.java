package com.udla.uyumbichoguard.config;

import com.udla.uyumbichoguard.model.Usuario;
import com.udla.uyumbichoguard.model.enums.EstadoUsuario;
import com.udla.uyumbichoguard.model.enums.RolUsuario;
import com.udla.uyumbichoguard.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer usuario ADMIN automáticamente al arrancar, SOLO si la
 * tabla usuarios está completamente vacía (instalación nueva). En
 * arranques posteriores no hace nada — es idempotente y seguro de
 * dejar activo permanentemente en producción.
 *
 * Sin esto, un despliegue nuevo no tendría forma de crear el primer
 * usuario (todos los endpoints de creación de usuario requieren ya
 * estar autenticado como ADMIN — problema del huevo y la gallina que
 * este componente resuelve).
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-inicial.cedula}")
    private String cedulaAdmin;

    @Value("${app.admin-inicial.nombres}")
    private String nombresAdmin;

    @Value("${app.admin-inicial.apellidos}")
    private String apellidosAdmin;

    @Value("${app.admin-inicial.email}")
    private String emailAdmin;

    @Value("${app.admin-inicial.password}")
    private String passwordAdmin;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.debug("Ya existen usuarios en el sistema; se omite la creación del ADMIN inicial.");
            return;
        }

        Usuario admin = Usuario.builder()
                .cedula(cedulaAdmin)
                .nombres(nombresAdmin)
                .apellidos(apellidosAdmin)
                .email(emailAdmin)
                .password(passwordEncoder.encode(passwordAdmin))
                .rol(RolUsuario.ADMIN)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepository.save(admin);

        log.warn("=================================================================");
        log.warn(" Usuario ADMIN inicial creado automáticamente:");
        log.warn(" Email: {}", emailAdmin);
        log.warn(" Contraseña: la definida en app.admin-inicial.password (application.yml)");
        log.warn(" CAMBIA ESTA CONTRASEÑA INMEDIATAMENTE tras el primer login.");
        log.warn("=================================================================");
    }
}