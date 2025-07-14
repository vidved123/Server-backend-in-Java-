package org.library.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final Algorithm algorithm;
    private final long EXPIRATION_TIME_MS;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:3600000}") long expirationTimeMs) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 32 characters long!");
        }

        this.algorithm = Algorithm.HMAC256(secret);
        this.EXPIRATION_TIME_MS = expirationTimeMs;

        // Log the secret (first and last 4 chars only) and expiration for debugging
        String masked = secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
        logger.info("[DEBUG] JWT Secret in use: {} (length: {})", masked, secret.length());
        logger.info("[DEBUG] JWT Expiration (ms): {}", expirationTimeMs);
    }

    public String generateToken(String username, String role) {
        logger.debug("🔍 JwtUtil - Generating token for username: {}, role: {}", username, role);
        
        String token = JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(algorithm);
        
        logger.debug("🔍 JwtUtil - Generated token: {}", token != null ? "TOKEN_PRESENT" : "NO_TOKEN");
        logger.debug("🔍 JwtUtil - Token length: {}", token != null ? token.length() : 0);
        
        return token;
    }

    public String extractUsername(String token) {
        try {
            return decodeToken(token).getSubject();
        } catch (JWTVerificationException e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public String extractUserRole(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getClaim("role").asString();
        } catch (JWTVerificationException e) {
            logger.warn("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = decodeToken(token).getExpiresAt();
            return expiration == null || expiration.before(new Date());
        } catch (JWTVerificationException e) {
            logger.warn("Token verification failed while checking expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            DecodedJWT jwt = decodeToken(token);
            boolean isValid = jwt.getSubject().equals(username) && !isTokenExpired(token);
            if (!isValid) {
                logger.info("Token validation failed for username: {}", username);
            }
            return isValid;
        } catch (JWTVerificationException e) {
            logger.warn("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public DecodedJWT decodeToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
}
