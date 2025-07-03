package org.library.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@SuppressWarnings("unused")
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final long EXPIRATION = 10 * 60 * 60 * 1000; // 10 hours

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long.");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        String token = JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
                .sign(algorithm);

        logger.debug("Generated token for user [{}] with role [{}]", userDetails.getUsername(), role);
        return token;
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            logger.warn("Token is null or blank.");
            return false;
        }

        try {
            DecodedJWT jwt = verifyToken(token);
            boolean valid = jwt.getExpiresAt().after(new Date());
            if (!valid) {
                logger.warn("Token expired at: {}", jwt.getExpiresAt());
            } else {
                logger.debug("Token is valid for user: {}", jwt.getSubject());
            }
            return valid;
        } catch (JWTVerificationException e) {
            logger.warn("Token verification failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return verifyToken(token).getSubject();
    }

    public DecodedJWT decodeToken(String token) {
        return verifyToken(token);
    }

    private DecodedJWT verifyToken(String token) {
        logger.debug("Verifying token...");
        return verifier.verify(token);
    }
}
