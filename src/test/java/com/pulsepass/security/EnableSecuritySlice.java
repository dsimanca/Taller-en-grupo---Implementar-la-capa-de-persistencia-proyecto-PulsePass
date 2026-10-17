package com.pulsepass.security;

import com.pulsepass.exception.GlobalExceptionHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Carga la cadena de filtros real de Spring Security en pruebas @WebMvcTest
 * sin deshabilitar filtros. Los tests deben configurar identidad con @WithMockUser
 * o un Bearer valido.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test")
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        RequestIdFilter.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
public @interface EnableSecuritySlice {
}
