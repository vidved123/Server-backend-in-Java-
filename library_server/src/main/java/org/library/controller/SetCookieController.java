package org.library.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class SetCookieController {

    @GetMapping("/set_cookie")
    public ResponseEntity<Map<String, String>> setCookie(HttpServletResponse response) {
        String token = "yourGeneratedTokenHere"; // 🔹 Replace with real token logic

        // ✅ Create cookie
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);

        // Set Secure to true if using HTTPS
        boolean isSecure = true; // You may want to use a config or environment check here
        cookie.setSecure(isSecure);

        cookie.setPath("/"); // Global access
        cookie.setAttribute("SameSite", "None"); // 🔹 Cross-origin support (with Secure=true)

        // Add cookie to response
        response.addCookie(cookie);

        // ✅ Response JSON
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Cookie set successfully");
        return ResponseEntity.ok(responseBody);
    }
}
