package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (properties.issuer() == null || properties.issuer().isBlank()) {
            throw new IllegalStateException("JWT issuer is required");
        }
        if (properties.expiration() == null || properties.expiration().isZero()
                || properties.expiration().isNegative()) {
            throw new IllegalStateException("JWT expiration must be positive");
        }
        if (properties.secretBase64() == null || properties.secretBase64().isBlank()) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 is required");
        }
        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secretBase64()));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT secret must be a valid Base64 HS256 key", exception);
        }
    }

    public String generateToken(SecurityPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder().subject(principal.userId().toString()).issuer(properties.issuer())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(properties.expiration())))
                .id(UUID.randomUUID().toString()).signWith(signingKey, Jwts.SIG.HS256).compact();
    }

    public Long extractUserId(String token) {
        var parsed = Jwts.parser().verifyWith(signingKey).requireIssuer(properties.issuer()).build()
                .parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(parsed.getHeader().getAlgorithm())) {
            throw new IllegalArgumentException("JWT algorithm is not allowed");
        }
        Claims claims = parsed.getPayload();
        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getIssuedAt() == null || claims.getId() == null || claims.getId().isBlank()) {
            throw new IllegalArgumentException("JWT is missing required claims");
        }
        if (claims.getIssuedAt().toInstant().isAfter(Instant.now().plusSeconds(60))) {
            throw new IllegalArgumentException("JWT issued-at time is in the future");
        }
        try {
            long userId = Long.parseLong(claims.getSubject());
            if (userId <= 0) {
                throw new IllegalArgumentException("JWT subject is invalid");
            }
            return userId;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("JWT subject is invalid", exception);
        }
    }
}
