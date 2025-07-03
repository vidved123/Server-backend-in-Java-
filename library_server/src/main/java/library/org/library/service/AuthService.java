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

    public void registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        logger.debug("🔐 Registering new user: {}", user.getUsername());

        validateUserFields(user);

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user
        userRepository.save(user);
        logger.info("✅ User '{}' registered successfully", user.getUsername());
    }

    public String loginUser(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("📩 Login attempt for user: {}", username);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("❌ Invalid credentials for user: {}", username);
            throw new RuntimeException("Invalid credentials");
        }

        logger.info("✅ Password verified for user: {}", username);

        // Set Spring Security Context manually
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        logger.debug("🎫 JWT token generated for user: {}", username);
        return token;
    }

    private void validateUserFields(User user) {
        if (isBlank(user.getFullName())) {
            throw new RuntimeException("Full name is required.");
        }
        if (isBlank(user.getMobileNumber())) {
            throw new RuntimeException("Mobile number is required.");
        }
        if (isBlank(user.getCountryCode())) {
            throw new RuntimeException("Country code is required.");
        }
    }
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
