package org.library.controller;

import org.library.entity.User;
import org.library.repository.UserRepository;
import org.library.service.LibraryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Year;
import java.util.Optional;

@Controller
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final UserRepository userRepository;

    public LibraryController(UserRepository userRepository, LibraryService libraryService) {
        this.libraryService = libraryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getLibraryPage(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";  // Redirect to log in if the user is not authenticated
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return "redirect:/login";  // Redirect if the user does not exist in the database
        }

        User user = userOpt.get();
        int totalBooks = libraryService.getTotalBookCount();
        var borrowedBooks = libraryService.getBorrowedBooksByUserId(user.getId());

        // Add model attributes
        model.addAttribute("bookCount", totalBooks);
        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userRole", user.getRole().name()); // Display role as a string (assuming it's an enum)
        model.addAttribute("year", Year.now().getValue());

        return "library";  // Return the name of the Thymeleaf view (library.html)
    }
}