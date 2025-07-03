package org.library.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SetCookieController {

    @GetMapping("/set_cookie")
    public ResponseEntity<Map<String, String>> setCookie(HttpServletRequest request,
                                                         HttpServletResponse response) {
        // 🔐 Replace this with real token generation logic
        String token = "yourGeneratedTokenHere";

        // ✅ Create secure cookie
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        boolean isSecure = isSecureRequest(request);
        cookie.setSecure(isSecure);

        // Only set SameSite=None if running over HTTPS
        if (isSecure) {
            cookie.setAttribute("SameSite", "None");
        }

        // 🧁 Add cookie to response
        response.addCookie(cookie);

        // ✅ Respond with JSON
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Cookie set successfully");
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Determines if the current request is secure (used to avoid Secure/SameSite=None on HTTP).
     */
    private boolean isSecureRequest(HttpServletRequest request) {
        String scheme = request.getScheme(); // http or https
        String serverName = request.getServerName(); // e.g., localhost, 127.0.0.1

        return scheme.equalsIgnoreCase("https")
                && !(serverName.equals("localhost") || serverName.equals("127.0.0.1"));
    }
}
