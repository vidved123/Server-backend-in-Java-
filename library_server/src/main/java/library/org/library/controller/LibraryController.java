package org.library.controller;

import java.time.Year;
import java.util.Optional;

import org.library.entity.User;
import org.library.repository.UserRepository;
import org.library.service.LibraryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        // Since /library is a public path, we handle both authenticated and unauthenticated users
        int totalBooks = libraryService.getTotalBookCount();
        
        // Add basic model attributes that don't require authentication
        model.addAttribute("bookCount", totalBooks);
        model.addAttribute("year", Year.now().getValue());
        
        // If user is authenticated, add user-specific data
        if (authentication != null && authentication.getName() != null) {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                var borrowedBooks = libraryService.getBorrowedBooksByUserId(user.getId());

                // Add user-specific model attributes
                model.addAttribute("username", user.getUsername());
                model.addAttribute("userRole", user.getRole().name());
                model.addAttribute("borrowedBooks", borrowedBooks);
                model.addAttribute("isAuthenticated", true);
            }
        } else {
            // For unauthenticated users, provide default values
            model.addAttribute("username", "Guest");
            model.addAttribute("userRole", "GUEST");
            model.addAttribute("borrowedBooks", java.util.Collections.emptyList());
            model.addAttribute("isAuthenticated", false);
        }

        return "library";  // Return the name of the Thymeleaf view (library.html)
    }
}
