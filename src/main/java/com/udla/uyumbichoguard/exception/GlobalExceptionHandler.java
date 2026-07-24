package com.udla.uyumbichoguard.exception;

import com.udla.uyumbichoguard.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * Punto único de manejo de excepciones para toda la API REST.
 *
 * Decisión de seguridad central: NINGÚN handler expone
 * exception.getMessage() de excepciones internas/técnicas
 * (DataIntegrityViolationException, NullPointerException, etc.) al
 * cliente — esos mensajes pueden contener detalles de la estructura de
 * la base de datos, nombres de columnas, o incluso fragmentos de SQL,
 * que son información valiosa para un atacante. Solo las excepciones
 * de NEGOCIO que nosotros mismos lanzamos (con mensajes redactados a
 * propósito) exponen su getMessage().
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoDuplicado(RecursoDuplicadoException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        // Excepción propia de Spring Security lanzada por
        // AuthenticationManager; la homogenizamos con nuestro mensaje
        // genérico en vez de exponer el texto interno de Spring.
        return construirRespuesta(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaBloqueada(CuentaBloqueadaException ex) {
        return construirRespuesta(HttpStatus.LOCKED, ex.getMessage());
    }

    @ExceptionHandler({AccesoDenegadoNegocioException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(RuntimeException ex) {
        // AccessDeniedException la lanza Spring Security cuando
        // @PreAuthorize rechaza una llamada (Parte 5/6).
        return construirRespuesta(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción");
    }

    @ExceptionHandler(VehiculoEnListaNegraException.class)
    public ResponseEntity<ErrorResponse> handleVehiculoEnListaNegra(VehiculoEnListaNegraException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Errores de validación de @Valid en DTOs de request (Bean
     * Validation). Aquí SÍ es seguro exponer detalles, porque son
     * errores del formato de entrada del propio cliente, no fugas de
     * información interna del sistema.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos",
                "Uno o más campos no cumplen las validaciones requeridas",
                detalles
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos",
                "Uno o más parámetros no cumplen las validaciones requeridas",
                detalles
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura violaciones de constraints a nivel de base de datos
     * (ej: unique constraint) que se escaparon de una validación
     * previa a nivel de servicio. Se registra el detalle técnico en
     * el log del servidor, pero al cliente solo se le informa que hay
     * un conflicto de datos, sin especificar cuál (evita filtrar
     * estructura de la BD).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.CONFLICT, "El recurso ya existe o viola una restricción de datos");
    }

    /**
     * Red de seguridad final: cualquier excepción no anticipada cae
     * aquí. Se registra completa en el log del servidor (con stack
     * trace, para depuración) pero al cliente se le da un mensaje
     * genérico de error 500 — NUNCA ex.getMessage() ni el stack trace,
     * que podrían revelar rutas de archivos, versiones de librerías,
     * o queries SQL.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerica(Exception ex, WebRequest request) {
        log.error("Error no controlado en {}: ", request.getDescription(false), ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intenta de nuevo más tarde.");
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje);
        return ResponseEntity.status(status).body(body);
    }
}