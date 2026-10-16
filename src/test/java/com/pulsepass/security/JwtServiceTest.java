package com.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtServiceTest {
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("pulsepass-test-hmac-key-32-bytes-long".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    @Test
    void createsAndValidatesTokenForPersistentUserId() {
        JwtService service = new JwtService(new JwtProperties("pulsepass-test", SECRET, Duration.ofMinutes(15)));
        SecurityPrincipal principal = new SecurityPrincipal(41L, "andrea", "andrea@email.com", "hash", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = service.generateToken(principal);

        assertThat(service.extractUserId(token)).isEqualTo(41L);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtService service = new JwtService(new JwtProperties("pulsepass-test", SECRET, Duration.ofMinutes(15)));
        JwtService otherService = new JwtService(new JwtProperties("pulsepass-test",
                Base64.getEncoder().encodeToString("another-test-hmac-key-at-least-32-bytes".getBytes(
                        java.nio.charset.StandardCharsets.UTF_8)), Duration.ofMinutes(15)));
        SecurityPrincipal principal = new SecurityPrincipal(41L, "andrea", "andrea@email.com", "hash", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThatThrownBy(() -> otherService.extractUserId(service.generateToken(principal)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsTokenFromAnotherIssuer() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        String token = Jwts.builder().subject("41").issuer("another-api")
                .issuedAt(Date.from(Instant.now())).expiration(Date.from(Instant.now().plusSeconds(900)))
                .id("test-token").signWith(key, Jwts.SIG.HS256).compact();
        JwtService service = new JwtService(new JwtProperties("pulsepass-test", SECRET, Duration.ofMinutes(15)));

        assertThatThrownBy(() -> service.extractUserId(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsExpiredToken() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        String token = Jwts.builder().subject("41").issuer("pulsepass-test")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60))).id("expired-token")
                .signWith(key, Jwts.SIG.HS256).compact();
        JwtService service = new JwtService(new JwtProperties("pulsepass-test", SECRET, Duration.ofMinutes(15)));

        assertThatThrownBy(() -> service.extractUserId(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsAWeakSigningKey() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("pulsepass-test",
                Base64.getEncoder().encodeToString("too-short".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                Duration.ofMinutes(15))))
                .isInstanceOf(IllegalStateException.class);
    }
}
