package org.library.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.library.dto.LoginRequest;
import org.library.entity.User;
import org.library.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ✅ Show Registration Page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ✅ Handle Registration POST
    @PostMapping("/register")
    public String handleRegistration(@ModelAttribute("user") User user, Model model, RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Registration failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";  // Redirect back to registration page
        }
    }

    // ✅ Show Login Page
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // ✅ Handle Login Submission
    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest request,
                        Model model,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        logger.info("📩 Received login request: {}", request);

        try {
            String token = authService.loginUser(request.getUsername(), request.getPassword());

            // Create JWT token cookie with Secure flag for HTTPS (ensure you're using HTTPS in production)
            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60); // 1 day
            tokenCookie.setSecure(true); // Secure flag (ensure HTTPS is used)
            response.addCookie(tokenCookie);

            redirectAttributes.addFlashAttribute("successMessage", "Login successful!");
            return "redirect:/dashboard";  // Redirect to dashboard after successful login
        } catch (Exception e) {
            logger.error("Login failed for user: {}", request.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "redirect:/login";  // Redirect back to login page on failure
        }
    }

    // ✅ Handle Logout
    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not logged in.");
            return "redirect:/login";
        }

        // Logout action in JWT-based systems: clear JWT token cookies
        clearCookie("token", response); // Clear JWT cookie

        // Optionally, clear other cookies if needed
        clearCookie("universal_token", response);

        // Redirect to login page after logout
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/login";
    }

    private void clearCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Make cookie expired immediately
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Ensure Secure flag is set for production
        response.addCookie(cookie); // Add to the response
    }
}
