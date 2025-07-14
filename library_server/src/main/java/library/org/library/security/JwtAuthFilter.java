package org.library.security;

import java.io.IOException;
import java.util.Collections;

import org.library.config.SecurityProperties;
import org.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SecurityProperties securityProperties;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        logger.info("🔧 JwtAuthFilter - Bean created and initialized");
        logger.info("🔧 JwtAuthFilter - SecurityProperties public paths: {}", securityProperties.getPublicPaths());
        
        // Check if /dashboard is in public paths
        boolean hasDashboard = securityProperties.getPublicPaths().contains("/dashboard");
        logger.info("🔧 JwtAuthFilter - Contains /dashboard in public paths: {}", hasDashboard);
        
        if (hasDashboard) {
            logger.warn("⚠️  JwtAuthFilter - WARNING: /dashboard is in public paths! JWT filter will be skipped for dashboard requests.");
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        boolean shouldSkip = securityProperties.getPublicPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
        
        logger.info("🔍 JWT Filter - Request path: {}, Should skip: {}", requestPath, shouldSkip);
        if (shouldSkip) {
            logger.info("🔍 JWT Filter - Skipping JWT filter for public path: {}", requestPath);
        }
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        logger.info("🔍 JWT Filter - Incoming request path: {}", request.getRequestURI());

        // Check if authentication is already present
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String name = (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
            logger.debug("Authentication already present: {}", name);
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        logger.debug("🔍 JWT Filter - Extracted token: {}", token != null ? "TOKEN_PRESENT" : "NO_TOKEN");

        if (token == null) {
            logger.debug("🔍 JWT Filter - No token found. Continuing without authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("🔍 JWT Filter - Token found, validating...");

        try {
            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
            String username = decodedJWT.getSubject();
            String role = decodedJWT.getClaim("role").asString();

            logger.debug("🔍 JWT Filter - Token validated for user: {}, role: {}", username, role);

            // Check if user exists in DB
            boolean userExists = userRepository.existsByUsername(username);
            logger.debug("🔍 JWT Filter - User '{}' exists in DB: {}", username, userExists);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("🔍 JWT Filter - Authentication set for user: {}", username);

        } catch (Exception e) {
            logger.warn("🔍 JWT Filter - Token validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        logger.debug("🔍 JWT Filter - Extracting token from request...");
        
        if (request.getCookies() != null) {
            logger.debug("🔍 JWT Filter - Found {} cookies", request.getCookies().length);
            for (Cookie cookie : request.getCookies()) {
                logger.debug("🔍 JWT Filter - Cookie: {} = {}", cookie.getName(), cookie.getValue() != null ? "VALUE_PRESENT" : "NULL");
                if ("token".equals(cookie.getName())) {
                    logger.debug("🔍 JWT Filter - Token found in cookie.");
                    return cookie.getValue();
                }
            }
        } else {
            logger.debug("🔍 JWT Filter - No cookies found in request");
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug("🔍 JWT Filter - Token found in Authorization header.");
            return header.substring(7);
        }

        logger.debug("🔍 JWT Filter - Token not found in cookies or Authorization header.");
        return null;
    }
}
