package com.pulsepass.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.pulsepass.exception.ErrorResponse;
import tools.jackson.databind.ObjectMapper;

final class SecurityResponses {

    private SecurityResponses() {
    }

    static void write(HttpServletResponse response,
                      ObjectMapper objectMapper,
                      HttpStatus status,
                      String error,
                      String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                Map.of());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
