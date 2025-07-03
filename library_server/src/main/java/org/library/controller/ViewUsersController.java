package org.library.controller;

import java.util.List;

import org.library.entity.User;
import org.library.service.UserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewUsersController {

    private final UserService userService;

    public ViewUsersController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Route: /view_users
    // Securing this route to be accessible only to users with the "ADMIN" role.
    @Secured("ROLE_ADMIN")
    @GetMapping("/view_users")
    public String viewUsersRoster(Model model) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
            return "users_roster"; // Name of the HTML file (users_roster.html) in templates folder

        } catch (Exception e) {
            // Log the exception for debugging purposes
            // Optionally, use a logger here
            model.addAttribute("error", "Could not load users: " + e.getMessage());
            return "error"; // Or redirect to a generic error page
        }
    }
}