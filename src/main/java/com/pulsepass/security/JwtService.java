package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
