package org.library.security;

import java.io.IOException;
import java.util.Collections;

import org.library.config.SecurityProperties;
import org.library.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SecurityProperties securityProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return securityProperties.getPublicPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        logger.debug("Incoming request path: {}", request.getRequestURI());

        String token = extractToken(request);

        logger.debug("Extracted token: {}", token != null ? token : "null");

        if (token == null) {
            logger.debug("No token found. Continuing without authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            logger.warn("Invalid or expired token.");
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("Authentication already present: {}",
                    SecurityContextHolder.getContext().getAuthentication().getName());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT jwt = jwtService.decodeToken(token);
            String username = jwt.getSubject();
            String rawRole = jwt.getClaim("role").asString();

            logger.debug("Decoded JWT - Username: {}, Role: {}", username, rawRole);

            if (username != null && rawRole != null) {
                String role = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
                var authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                var userDetails = new org.springframework.security.core.userdetails.User(
                        username, "", authorities
                );

                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                logger.debug("Authentication set in SecurityContext for user: {}", username);
            } else {
                logger.warn("JWT missing username or role.");
            }
        } catch (Exception e) {
            logger.error("Exception during authentication setup", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    logger.debug("Token found in cookie.");
                    return cookie.getValue();
                }
            }
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug("Token found in Authorization header.");
            return header.substring(7);
        }

        logger.debug("Token not found in cookies or Authorization header.");
        return null;
    }
}
