package org.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey SECRET_KEY;
    private final long EXPIRATION_TIME_MS;

    // Constructor to initialize SECRET_KEY and EXPIRATION_TIME_MS
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration.ms:3600000}") long expirationTimeMs) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 32 bytes long!");
        }

        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
        this.EXPIRATION_TIME_MS = expirationTimeMs;
    }

    // Generate JWT token with claims and role
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);  // Add role as a claim
        return createToken(claims, username);
    }

    // Create token with specific claims and subject (username)
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS)) // Set expiration
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)  // Sign with secret key
                .compact();
    }

    // Extract username from the token
    public String extractUsername(String token) {
        return extractClaims(token, SECRET_KEY).getSubject();
    }

    // Check if the token has expired
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaims(token, SECRET_KEY).getExpiration();
            return expiration.before(new Date()); // Return true if expired
        } catch (ExpiredJwtException e) {
            // Log or handle expired token case here if needed
            return true; // Token expired
        } catch (JwtException e) {
            // Log or handle other parsing exceptions here if needed
            return false;
        }
    }

    // Validate the token by checking if username matches and token isn't expired
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);  // Valid token
        } catch (JwtException e) {
            // Handle invalid token case (e.g. malformed, expired)
            return false;
        }
    }

    // Reusable method to extract claims from the token
    public Claims extractClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
