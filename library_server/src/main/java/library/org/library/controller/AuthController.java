package org.library.controller;

import org.library.dto.LoginRequest;
import org.library.entity.User;
import org.library.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle user registration
    @PostMapping("/register")
    public String handleRegistration(@ModelAttribute("user") User user,
                                     RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Registration failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // Handle login
    @PostMapping("/auth/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        logger.info("📩 Received login request: {}", request.getUsername());

        try {
            String token = authService.loginUser(request.getUsername(), request.getPassword());
            logger.debug("🔍 AuthController - Generated JWT token: {}", token != null ? "TOKEN_PRESENT" : "NO_TOKEN");

            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60); // 1 day

            boolean isSecure = isSecureRequest(httpRequest);
            tokenCookie.setSecure(isSecure);
            String serverName = httpRequest.getServerName();
            if (isSecure) {
                tokenCookie.setAttribute("SameSite", "None");
            } else {
                // Always set SameSite=Lax for localhost/127.0.0.1
                if ("localhost".equals(serverName) || "127.0.0.1".equals(serverName)) {
                    tokenCookie.setAttribute("SameSite", "Lax");
                }
            }

            logger.debug("🔍 AuthController - Setting cookie - Name: {}, Value: {}, Secure: {}, Path: {}", 
                       tokenCookie.getName(), tokenCookie.getValue() != null ? "VALUE_PRESENT" : "NULL", 
                       tokenCookie.getSecure(), tokenCookie.getPath());

            response.addCookie(tokenCookie);

            redirectAttributes.addFlashAttribute("successMessage", "Login successful!");
            logger.info("🔍 AuthController - Login successful, redirecting to dashboard");
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Login failed for user: {}", request.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "redirect:/login";
        }
    }

    // Handle logout
    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            logger.warn("Logout attempted without an authenticated user.");
            redirectAttributes.addFlashAttribute("errorMessage", "You are not logged in.");
            return "redirect:/login";
        }

        clearCookie("token", request, response);
        clearCookie("universal_token", request, response);

        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/login";
    }

    // Clear cookie helper
    private void clearCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureRequest(request));
        response.addCookie(cookie);
    }

    // Detects if HTTPS is used
    private boolean isSecureRequest(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        // Allow HTTP for localhost/127.0.0.1 development
        return scheme.equalsIgnoreCase("https") && 
               !serverName.contains("localhost") && 
               !serverName.contains("127.0.0.1");
    }
}
