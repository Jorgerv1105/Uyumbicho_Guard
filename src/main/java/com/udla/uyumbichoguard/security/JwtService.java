package com.udla.uyumbichoguard.security;

import com.udla.uyumbichoguard.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Encapsula la generación y validación de JWT con jjwt 0.12.6.
 *
 * Decisiones de seguridad:
 * - Algoritmo HMAC-SHA256 (simétrico): apropiado porque el mismo
 *   backend emite y valida los tokens (no hay necesidad de un par
 *   asimétrico público/privado para terceros).
 * - Se incluyen claims mínimos y no sensibles (id, rol) para poder
 *   autorizar sin golpear la base de datos en cada request. NUNCA se
 *   incluye la contraseña ni datos personales sensibles en el payload,
 *   ya que el JWT solo está firmado, no cifrado, y es legible por
 *   cualquiera que lo intercepte (Base64, no encriptación).
 * - El token de refresco es un JWT separado y más simple (solo subject
 *   + expiración), para reducir superficie si se filtra.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Genera el access token con claims adicionales (id y rol del
     * usuario) para que el filtro de autorización no necesite consultar
     * la base de datos en cada request protegido.
     */
    public String generateAccessToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usuario.getId());
        claims.put("rol", usuario.getRol().name());
        claims.put("nombres", usuario.getNombres());
        return buildToken(claims, usuario.getEmail(), jwtProperties.getExpirationMs());
    }

    /**
     * Genera el refresh token. Sin claims extra a propósito: su único
     * uso es solicitar un nuevo access token, no autorizar directamente
     * operaciones de negocio.
     */
    public String generateRefreshToken(Usuario usuario) {
        return buildToken(new HashMap<>(), usuario.getEmail(), jwtProperties.getRefreshExpirationMs());
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el email (subject) del token sin necesidad de tener el
     * UserDetails a mano — usado por el filtro de autenticación.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Valida que el token pertenezca al usuario esperado y no haya
     * expirado. La verificación de firma ocurre implícitamente al
     * parsear (parseSignedClaims lanza excepción si la firma no
     * coincide), por eso no hay una comprobación de firma explícita.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parsea y valida la firma del token. Si la firma no es válida o el
     * token está corrupto, jjwt lanza una excepción no chequeada
     * (JwtException) que es capturada en JwtAuthenticationFilter.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Reconstruye la SecretKey a partir del secreto Base64 configurado.
     * No se cachea como campo de instancia porque JwtProperties podría
     * recargarse (poco probable en prod, pero es una práctica segura
     * y el costo de decodificar Base64 es despreciable).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Wrapper explícito para capturar expiración en el filtro sin
     * exponer la excepción cruda de jjwt en capas superiores.
     */
    public boolean isTokenSyntacticallyValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}