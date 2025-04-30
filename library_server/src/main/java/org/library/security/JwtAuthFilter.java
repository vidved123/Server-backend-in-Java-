package org.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger jwtAuthLogger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.universalSecret}")
    private String universalSecret;

    private SecretKey jwtSecretKey;
    private SecretKey universalSecretKey;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void initKeys() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.universalSecretKey = Keys.hmacShaKeyFor(universalSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String path = request.getServletPath();
        jwtAuthLogger.info("🔍 Intercepted request: {}", path);

        // Skip the filter for certain paths (including logout)
        if (shouldNotFilter(request)) {
            jwtAuthLogger.debug("➡️ Skipping filter for path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            jwtAuthLogger.warn("🚫 No token found in request.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is missing!");
            return;
        }

        try {
            // Handle the case where the token is expired, especially for logout requests
            if (path.equals("/logout")) {
                // Token may be expired, but we allow logout to proceed
                jwtAuthLogger.info("🔑 Allowing logout regardless of token expiry.");
                clearTokenFromCookies(response);
                chain.doFilter(request, response);
                return;
            }

            SecretKey key = isUniversalToken(token) ? universalSecretKey : jwtSecretKey;
            Claims claims = jwtUtil.extractClaims(token, key);

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            if (role == null) role = "USER"; // Ensure the role is either provided or default to USER.

            if (!jwtUtil.validateToken(token, username)) {
                jwtAuthLogger.warn("❌ Token validation failed for user: {}", username);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token!");
                return;
            }

            // If token is expired, log the user out and clear context
            if (jwtUtil.isTokenExpired(token)) {
                jwtAuthLogger.warn("⏰ Token expired, logging out user: {}", username);
                clearTokenFromCookies(response);
                SecurityContextHolder.clearContext();  // Clear user authentication
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired, logged out!");
                return;
            }

            UserDetails userDetails = User.withUsername(username)
                    .password("") // Password not needed for context
                    .roles(role.toUpperCase())
                    .build();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            jwtAuthLogger.info("✅ Authenticated user: {} with role: {}", username, role);

        } catch (ExpiredJwtException e) {
            jwtAuthLogger.error("⏰ Token expired: {}", e.getMessage());
            clearTokenFromCookies(response);
            SecurityContextHolder.clearContext();  // Clear user authentication on expiry
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired, logged out!");
            return;
        } catch (JwtException e) {
            jwtAuthLogger.error("💥 Token parsing failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid!");
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // First check cookies for tokens
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName()) || "universal_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Fallback to Authorization header if no cookie found
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private boolean isUniversalToken(String token) {
        return token.startsWith("universal_");
    }

    private void clearTokenFromCookies(HttpServletResponse response) {
        // Clear the JWT token from cookies
        Cookie tokenCookie = new Cookie("token", null);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);  // Expire the cookie
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);  // Set Secure flag for HTTPS
        response.addCookie(tokenCookie);

        Cookie universalTokenCookie = new Cookie("universal_token", null);
        universalTokenCookie.setPath("/");
        universalTokenCookie.setMaxAge(0);  // Expire the cookie
        universalTokenCookie.setHttpOnly(true);
        universalTokenCookie.setSecure(true);  // Set Secure flag for HTTPS
        response.addCookie(universalTokenCookie);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip the filter for login, register, logout, and other paths that don't need authentication
        String path = request.getRequestURI();
        return path.equals("/") ||
                path.equals("/login") ||
                path.equals("/register") ||
                path.equals("/favicon.ico") ||
                path.equals("/logout");
    }
}
