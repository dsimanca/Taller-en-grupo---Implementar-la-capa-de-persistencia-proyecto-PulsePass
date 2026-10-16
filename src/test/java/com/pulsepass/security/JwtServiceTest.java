package com.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.domain.enums.Role;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

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
        JwtService expired = new JwtService(
                new JwtProperties("pulsepass-api", SECRET, Duration.ofSeconds(-60)));
        String token = expired.generateToken(principal());

        assertThatThrownBy(() -> expired.extractUserId(token)).isInstanceOf(JwtException.class);
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
}
