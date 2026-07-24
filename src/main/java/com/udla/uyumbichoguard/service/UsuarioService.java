package com.udla.uyumbichoguard.service;

import com.udla.uyumbichoguard.dto.request.ActualizarUsuarioRequest;
import com.udla.uyumbichoguard.dto.request.CambiarPasswordRequest;
import com.udla.uyumbichoguard.dto.request.CambiarRolRequest;
import com.udla.uyumbichoguard.dto.request.CrearUsuarioRequest;
import com.udla.uyumbichoguard.dto.response.UsuarioResponse;
import com.udla.uyumbichoguard.exception.AccesoDenegadoNegocioException;
import com.udla.uyumbichoguard.exception.CredencialesInvalidasException;
import com.udla.uyumbichoguard.exception.RecursoDuplicadoException;
import com.udla.uyumbichoguard.exception.RecursoNoEncontradoException;
import com.udla.uyumbichoguard.model.Usuario;
import com.udla.uyumbichoguard.model.enums.EstadoUsuario;
import com.udla.uyumbichoguard.model.enums.RolUsuario;
import com.udla.uyumbichoguard.repository.UsuarioRepository;
import com.udla.uyumbichoguard.security.AutenticacionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión de cuentas de usuario del sistema.
 *
 * Decisión de diseño (Parte 3): "actualizar perfil" y "cambiar rol"
 * son operaciones DELIBERADAMENTE separadas — la primera la puede
 * hacer el propio usuario, la segunda es exclusiva de ADMIN. Mezclar
 * ambas en un solo endpoint permitiría a un usuario auto-otorgarse
 * privilegios si el frontend tuviera un bug, así que la separación es
 * una medida de seguridad, no solo de organización de código.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con ese email");
        }
        if (usuarioRepository.existsByCedula(request.cedula())) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con esa cédula");
        }

        Usuario usuario = Usuario.builder()
                .cedula(request.cedula())
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .email(request.email())
                // BCrypt cost 12 configurado centralmente en SecurityConfig (Parte 2)
                .password(passwordEncoder.encode(request.password()))
                .telefono(request.telefono())
                .rol(request.rol())
                .estado(EstadoUsuario.ACTIVO)
                .build();

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.desde(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfilActual() {
        return UsuarioResponse.desde(AutenticacionUtils.usuarioAutenticado());
    }

    /**
     * Un usuario puede actualizar su propio perfil; un ADMIN puede
     * actualizar el de cualquiera. La verificación depende del ID del
     * recurso solicitado, por eso se hace en código y no solo con
     * @PreAuthorize (que no tiene forma limpia de comparar el path
     * variable contra el usuario autenticado sin SpEL complejo).
     */
    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarOFallar(id);
        verificarPropioOAdmin(id);

        if (request.nombres() != null) usuario.setNombres(request.nombres());
        if (request.apellidos() != null) usuario.setApellidos(request.apellidos());
        if (request.telefono() != null) usuario.setTelefono(request.telefono());

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordRequest request) {
        Usuario usuario = buscarOFallar(id);
        verificarPropioOAdmin(id);

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("La contraseña actual no es correcta");
        }

        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UsuarioResponse cambiarRol(Long id, CambiarRolRequest request) {
        Usuario usuario = buscarOFallar(id);
        usuario.setRol(request.rol());
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        Usuario usuario = buscarOFallar(id);
        usuario.setEstado(nuevoEstado);
        // Si se reactiva manualmente, limpiamos cualquier bloqueo
        // temporal por intentos fallidos para que el cambio sea efectivo
        // de inmediato (de lo contrario seguiría bloqueado hasta que
        // expire fechaBloqueoHasta, contradiciendo la acción del ADMIN).
        if (nuevoEstado == EstadoUsuario.ACTIVO) {
            usuario.setIntentosFallidos(0);
            usuario.setFechaBloqueoHasta(null);
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    private Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    private void verificarPropioOAdmin(Long idRecurso) {
        Usuario actual = AutenticacionUtils.usuarioAutenticado();
        boolean esAdmin = actual.getRol() == RolUsuario.ADMIN;
        boolean esElMismo = actual.getId().equals(idRecurso);
        if (!esAdmin && !esElMismo) {
            throw new AccesoDenegadoNegocioException("No tienes permisos para modificar este recurso");
        }
    }
}