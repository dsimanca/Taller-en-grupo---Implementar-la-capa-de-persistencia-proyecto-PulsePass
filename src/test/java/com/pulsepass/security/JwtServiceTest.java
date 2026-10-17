package com.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.domain.enums.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    private JwtService service() {
        return new JwtService(new JwtProperties("pulsepass-api", SECRET, Duration.ofMinutes(15)));
    }

    private SecurityPrincipal principal() {
        return new SecurityPrincipal(7L, "andrea", "andrea@email.com", "$2a$hash", true, List.of(Role.USER));
    }

    @Test
    void generatesATokenThatResolvesTheUserId() {
        JwtService jwtService = service();

        String token = jwtService.generateToken(principal());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUserId(token)).isEqualTo("7");
    }

    @Test
    void reportsTheAccessTokenTtlInSeconds() {
        assertThat(service().accessTokenTtlSeconds()).isEqualTo(900L);
    }

    @Test
    void rejectsAnExpiredToken() {
        String token = Jwts.builder()
                .subject("7")
                .issuer("pulsepass-api")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .id(UUID.randomUUID().toString())
                .signWith(SIGNING_KEY, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> service().extractUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() {
        JwtService issuer = service();
        JwtService other = new JwtService(new JwtProperties(
                "pulsepass-api",
                "YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk=",
                Duration.ofMinutes(15)));
        String token = other.generateToken(principal());

        assertThatThrownBy(() -> issuer.extractUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsATokenFromAnotherIssuer() {
        JwtService issuer = service();
        JwtService foreign = new JwtService(
                new JwtProperties("otro-issuer", SECRET, Duration.ofMinutes(15)));
        String token = foreign.generateToken(principal());

        assertThatThrownBy(() -> issuer.extractUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsATamperedToken() {
        JwtService jwtService = service();
        String token = jwtService.generateToken(principal());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.extractUserId(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTokensMissingRequiredClaims() {
        JwtService jwtService = service();
        String token = Jwts.builder()
                .subject("7")
                .issuer("pulsepass-api")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SIGNING_KEY, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsAnUnexpectedSigningAlgorithm() {
        String longSecret = Base64.getEncoder().encodeToString(new byte[48]);
        JwtService verifier = new JwtService(
                new JwtProperties("pulsepass-api", longSecret, Duration.ofMinutes(15)));
        SecretKey unexpectedAlgorithmKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(longSecret));
        String token = Jwts.builder()
                .subject("7")
                .issuer("pulsepass-api")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .id(UUID.randomUUID().toString())
                .signWith(unexpectedAlgorithmKey, Jwts.SIG.HS384)
                .compact();

        assertThatThrownBy(() -> verifier.extractUserId(token)).isInstanceOf(UnsupportedJwtException.class);
    }

    @Test
    void rejectsANonNumericSubject() {
        String token = Jwts.builder()
                .subject("andrea")
                .issuer("pulsepass-api")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .id(UUID.randomUUID().toString())
                .signWith(SIGNING_KEY, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> service().extractUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void failsFastWhenTheSecretIsMissing() {
        assertThatThrownBy(() -> new JwtService(
                new JwtProperties("pulsepass-api", "", Duration.ofMinutes(15))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failsFastWhenTheSecretIsTooShort() {
        assertThatThrownBy(() -> new JwtService(
                new JwtProperties("pulsepass-api", "c2hvcnQ=", Duration.ofMinutes(15))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsANonPositiveTokenTtl() {
        assertThatThrownBy(() -> new JwtProperties("pulsepass-api", SECRET, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
