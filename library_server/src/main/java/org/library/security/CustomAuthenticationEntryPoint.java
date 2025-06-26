package org.library.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Return 401 Unauthorized when authentication is required
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // Set status code to 401
        response.setContentType("application/json");  // Set content type to JSON

        // Custom error message in JSON format
        String jsonResponse = "{\"error\": \"Unauthorized access\", \"message\": \"Authentication required\"}";

        // Write the error message to the response body
        response.getWriter().write(jsonResponse);
    }
}