package com.pulsepass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var authorizationHeaders = Collections.list(request.getHeaders("Authorization"));
        if (authorizationHeaders.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (authorizationHeaders.size() != 1
                    || !authorizationHeaders.get(0).regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())
                    || authorizationHeaders.get(0).length() <= "Bearer ".length()) {
                throw new BadCredentialsException("Invalid bearer token");
            }
            String token = authorizationHeaders.get(0).substring("Bearer ".length()).trim();
            if (token.isEmpty() || token.contains(" ")) {
                throw new BadCredentialsException("Invalid bearer token");
            }

            Long userId = jwtService.extractUserId(token);
            if (userId == null || userId <= 0) {
                throw new BadCredentialsException("Invalid bearer token");
            }
            SecurityPrincipal principal = userDetailsService.loadByUserId(userId);
            if (principal == null || !principal.isEnabled()) {
                throw new BadCredentialsException("Invalid bearer token");
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BadCredentialsException | UsernameNotFoundException | io.jsonwebtoken.JwtException
                 | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Invalid bearer token", exception));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
