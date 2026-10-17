package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final int MIN_KEY_BYTES = 32;

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = buildSigningKey(properties.secretBase64());
    }

    private SecretKey buildSigningKey(String secretBase64) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException(
                    "PULSEPASS_JWT_SECRET_BASE64 is required; a valid Base64 secret must be configured");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretBase64);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 is not valid Base64", ex);
        }
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "PULSEPASS_JWT_SECRET_BASE64 must decode to at least " + MIN_KEY_BYTES + " bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(SecurityPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.accessTokenTtl());
        List<String> roles = principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .sorted()
                .toList();
        return Jwts.builder()
                .subject(principal.getId().toString())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }

    public String extractUserId(String token) {
        String subject = extractAllClaims(token).getSubject();
        try {
            if (Long.parseLong(subject) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new MalformedJwtException("JWT subject must be a positive user ID", ex);
        }
        return subject;
    }

    private Claims extractAllClaims(String token) {
        Jws<Claims> signedClaims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(signedClaims.getHeader().getAlgorithm())) {
            throw new UnsupportedJwtException("Only HS256 tokens are accepted");
        }
        Claims claims = signedClaims.getPayload();
        if (claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getIssuedAt() == null
                || claims.getExpiration() == null
                || claims.getId() == null || claims.getId().isBlank()) {
            throw new MalformedJwtException("JWT is missing required claims");
        }
        if (claims.getIssuedAt().toInstant().isAfter(Instant.now())) {
            throw new MalformedJwtException("JWT issued-at time is in the future");
        }
        return claims;
    }
}
