package org.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        boolean loggedIn = false;

        // ✅ Check for token in cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    loggedIn = true;
                    break;
                }
            }
        }

        // ✅ Pass authentication status to Thymeleaf
        model.addAttribute("logged_in", loggedIn);
        return "home"; // ✅ Renders `home.html`
    }

    // ✅ Allow /home to also redirect to the homepage
    @GetMapping("/home")
    public String redirectToRoot() {
        return "redirect:/";
    }

    // ✅ Logout: Deletes the cookie and redirects
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);  // Immediately expires the cookie
        cookie.setPath("/");   // Make sure the path is the same as where the cookie was set
        cookie.setHttpOnly(true);  // Make the cookie inaccessible via JavaScript
        cookie.setSecure(true);  // Ensure the cookie is sent over HTTPS (change this based on your environment)

        // Optional: Set domain if needed for subdomains (e.g., ".yourdomain.com")
        // cookie.setDomain(".yourdomain.com");

        response.addCookie(cookie);
        return "redirect:/";
    }
}