package com.rentaherramientas.platform.security;

import com.rentaherramientas.api.security.AuthenticatedUser;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.model.Usuario;
import com.rentaherramientas.domain.port.out.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

/** Adaptador de salida: emite y valida tokens JWT firmados con HMAC-SHA. */
@Component
public class JwtService implements TokenProviderPort {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${app.jwt.secret}") String secretoBase64,
                      @Value("${app.jwt.expiration-minutes}") long expiracionMinutos) {
        byte[] bytes = Decoders.BASE64.decode(secretoBase64);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 bytes (256 bits) en Base64");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMinutos = expiracionMinutos;
    }

    @Override
    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim("email", usuario.getEmail())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(Duration.ofMinutes(expiracionMinutos))))
                .signWith(clave)
                .compact();
    }

    @Override
    public long expiracionSegundos() {
        return Duration.ofMinutes(expiracionMinutos).toSeconds();
    }

    /** Devuelve el usuario del token, o vacío si está vencido, manipulado o mal formado. */
    public Optional<AuthenticatedUser> validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    Rol.valueOf(claims.get(CLAIM_ROL, String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
