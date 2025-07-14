package org.library.controller;

import java.util.List;
import java.util.Map;

import org.library.dto.ViewBorrowedBooksdto;
import org.library.entity.enums.Role;
import org.library.service.UserService;
import org.library.service.ViewBorrowedBooksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view_borrowed_books")
public class ViewBorrowedBooksController {

    private static final Logger logger = LoggerFactory.getLogger(ViewBorrowedBooksController.class);

    @Autowired
    private ViewBorrowedBooksService viewService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewBorrowedBooks(Authentication authentication, Model model,
                                    @RequestParam(required = false) String searchQuery) {
        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("error", "User not logged in.");
            return "redirect:/login"; // Redirect to login page
        }

        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);

        // Extract role from authorities and validate it
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        String cleanRole = role.replace("ROLE_", ""); // Remove prefix
        try {
            Role.valueOf(cleanRole); // Validate role
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid role: " + cleanRole);
            logger.error("Invalid role: {}", cleanRole);
            return "view_borrowed_books";
        }

        List<ViewBorrowedBooksdto> borrowedBooks;

        // Search functionality: If there's a search query, filter the books
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            borrowedBooks = viewService.searchBorrowedBooks(searchQuery.trim().toLowerCase(), userId, cleanRole);
        } else {
            borrowedBooks = viewService.getBorrowedBooks(userId, cleanRole); // Default method to get all borrowed books
        }

        // Add attributes for the view
        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("role", cleanRole);
        model.addAttribute("searchQuery", searchQuery); // Maintain search query
        model.addAttribute("messages", Map.of("success", "Borrowed books loaded."));

        // Log the loaded books info
        logger.info("Borrowed books loaded for user '{}' with role '{}', search query '{}'", username, cleanRole, searchQuery);

        return "view_borrowed_books"; // Return to the same page
    }
}
