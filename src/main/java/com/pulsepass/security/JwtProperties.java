package com.pulsepass.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsepass.security.jwt")
public record JwtProperties(String issuer, String secretBase64, Duration expiration) {
}
