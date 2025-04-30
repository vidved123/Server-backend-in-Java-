package org.library.controller;

import org.library.dto.ReturnBooksdto;
import org.library.entity.enums.Role;
import org.library.service.ReturnBooksService;
import org.library.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/return_books")
public class ReturnBooksController {

    private static final Logger logger = LoggerFactory.getLogger(ReturnBooksController.class);

    @Autowired
    private ReturnBooksService returnBooksService;

    @Autowired
    private UserService userService;

    // Show the return books page
    @GetMapping
    public String showReturnPage(
            Authentication authentication,
            Model model,
            @RequestParam(value = "userId", required = false) Long userIdInput) {

        // Check if the user is authenticated
        if (authentication == null || authentication.getName() == null) {
            logger.warn("Unauthorized access attempt to return page.");
            model.addAttribute("error", "User is not logged in.");
            return "redirect:/login"; // Redirect to login page instead of showing error
        }

        // Get the current user details
        String username = authentication.getName();
        Long currentUserId = userService.getUserIdByUsername(username);
        Role role = userService.getUserRoleByUsername(username);

        Long targetUserId;

        // Admin logic
        if (role == Role.ADMIN) {
            if (userIdInput != null) {
                // Validate if the user ID exists
                if (!userService.existsById(userIdInput)) {
                    model.addAttribute("error", "User ID not found.");
                    model.addAttribute("borrowedBooks", new ArrayList<>());
                    model.addAttribute("role", role);
                    return "return_books";
                }
                targetUserId = userIdInput;
            } else {
                targetUserId = null; // Admin shows all borrowed books
            }
        } else {
            targetUserId = currentUserId; // Regular users can only see their own books
        }

        List<ReturnBooksdto> borrowedBooks = returnBooksService.getBorrowedBooks(targetUserId, role.name());
        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("role", role);

        return "return_books";
    }

    // Handle book return logic
    @PostMapping
    public String returnBooks(
            @RequestParam(value = "bookIds", required = false) List<Long> bookIds,
            @RequestParam(value = "bookIdsAndUserIds", required = false) List<String> bookIdsAndUserIds,
            Authentication authentication,
            Model model) {

        // Check if the user is authenticated
        if (authentication == null || authentication.getName() == null) {
            logger.warn("Unauthorized return attempt.");
            model.addAttribute("error", "User is not logged in.");
            return "redirect:/login"; // Redirect to login page
        }

        // Get the current user details
        String username = authentication.getName();
        Long currentUserId = userService.getUserIdByUsername(username);
        Role role = userService.getUserRoleByUsername(username);

        List<Long> finalBookIds = new ArrayList<>();
        List<Long> userIds = null;

        // If the user is an admin, process bookIds and userIds together
        if (role == Role.ADMIN) {
            if (bookIdsAndUserIds != null) {
                finalBookIds = parseBookIdsAndUserIds(bookIdsAndUserIds, model);
            }
        } else {
            // For non-admin, process only the current user's books
            finalBookIds = bookIds != null ? bookIds : new ArrayList<>();
        }

        try {
            // Call the service to process the book return
            returnBooksService.returnBooks(finalBookIds, null, currentUserId, role.name());
            model.addAttribute("message", "Books returned successfully.");
            logger.info("✅ Books returned by user '{}'", username);
        } catch (Exception e) {
            // Log any errors that occur during the book return process
            logger.error("❌ Error returning books: {}", e.getMessage(), e);
            model.addAttribute("error", "Error returning books: " + e.getMessage());
        }

        // Refresh the list of borrowed books after returning them
        List<ReturnBooksdto> borrowedBooks = returnBooksService.getBorrowedBooks(currentUserId, role.name());
        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("role", role);

        return "return_books";
    }

    // Extracts and returns the final list of book IDs from the provided list of bookIds and userIds
    private List<Long> parseBookIdsAndUserIds(List<String> bookIdsAndUserIds, Model model) {
        List<Long> finalBookIds = new ArrayList<>();

        for (String pair : bookIdsAndUserIds) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                try {
                    finalBookIds.add(Long.parseLong(parts[0]));
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "Invalid book ID format.");
                    logger.error("Invalid book ID format in bookIdsAndUserIds: {}", pair);
                }
            } else {
                model.addAttribute("error", "Invalid format for book and user IDs.");
                logger.error("Invalid format for book and user IDs: {}", pair);
            }
        }

        return finalBookIds;
    }
}
