package org.library.service;

import org.library.entity.User;
import org.library.repository.UserRepository;
import org.library.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user if the username doesn't already exist.
     */
    public void registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        // Log before encoding password
        logger.debug("Registering new user: {}", user);

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Important: Make sure fields like fullName, mobileNumber, and countryCode are NOT null
        validateUserFields(user);

        userRepository.save(user);
        logger.info("✅ Successfully registered user: {}", user.getUsername());
    }

    /**
     * Log in a user and return a JWT token.
     */
    public String loginUser(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("📩 Login attempt for user: {}", username);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.error("❌ Invalid credentials for user: {}", username);
            throw new RuntimeException("Invalid credentials");
        }

        logger.info("✅ Password verified for user: {}", username);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    /**
     * Utility method to ensure user has required fields.
     */
    private void validateUserFields(User user) {
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            throw new RuntimeException("Full name is required.");
        }
        if (user.getMobileNumber() == null || user.getMobileNumber().isBlank()) {
            throw new RuntimeException("Mobile number is required.");
        }
        if (user.getCountryCode() == null || user.getCountryCode().isBlank()) {
            throw new RuntimeException("Country code is required.");
        }
    }
}
