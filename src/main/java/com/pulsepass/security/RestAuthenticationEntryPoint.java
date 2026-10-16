package com.pulsepass.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsepass.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);
    private static final String REQUEST_ID_KEY = "requestId";
    private final ObjectMapper objectMapper;
    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    @Override public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        logger.warn("Authentication denied requestId={}", MDC.get(REQUEST_ID_KEY));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(LocalDateTime.now(), 401, "Unauthorized", "Authentication is required", Map.of()));
    }
}
