package org.library.controller;

import java.util.Optional;

import org.library.entity.User;
import org.library.entity.enums.Sex;
import org.library.exception.UserNotFoundException;
import org.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    // ✅ Show profile page
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication,
                              Model model,
                              HttpServletResponse response) {

        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        // 🚫 Prevent caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Retrieve user and throw exception if not found
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found with username: " + authentication.getName());
        }

        model.addAttribute("user", userOptional.get());
        return "profile";
    }

    // ✅ Handle profile update
    @PostMapping("/update_profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String fullName,
                                @RequestParam String sex,
                                @RequestParam String mobileNumber,
                                @RequestParam String countryCode,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        // Retrieve user and throw exception if not found
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        if (userOptional.isEmpty()) {
            return "redirect:/login"; // You may redirect to log in if user is not found
        }

        User user = userOptional.get();

        // ✅ Validate required fields
        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || mobileNumber == null || mobileNumber.isBlank()
                || countryCode == null || countryCode.isBlank()) {

            model.addAttribute("error", "All fields are required.");
            model.addAttribute("user", user);
            return "profile";
        }

        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setMobileNumber(mobileNumber.trim());
        user.setCountryCode(countryCode.trim());

        try {
            user.setSex(Sex.valueOf(sex.toUpperCase()));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid gender selected.");
            model.addAttribute("user", user);
            return "profile";
        }

        try {
            userService.updateUser(user.getId(), user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
            model.addAttribute("user", user);
            return "profile";
        }

        return "redirect:/profile";
    }
}
