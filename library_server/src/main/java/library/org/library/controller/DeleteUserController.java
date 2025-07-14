package org.library.controller;

import java.util.List;
import java.util.Optional;

import org.library.entity.User;
import org.library.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/delete_user")
public class DeleteUserController {

    private final UserRepository userRepository;

    public DeleteUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Show delete user page
    @GetMapping
    public String showDeleteUserPage(Authentication authentication, Model model,
                                     RedirectAttributes redirectAttributes) {

        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("message", "Access denied.");
            redirectAttributes.addFlashAttribute("messageCategory", "danger");
            return "redirect:/dashboard";
        }

        List<User> allUsers = userRepository.findAll();
        model.addAttribute("users", allUsers);
        return "delete_user";
    }

    // ✅ Handle delete user form submission
    @PostMapping
    public String handleDeleteUser(@RequestParam("user_id") Long userId,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("message", "Access denied.");
            redirectAttributes.addFlashAttribute("messageCategory", "danger");
            return "redirect:/dashboard";
        }

        Optional<User> userToDelete = userRepository.findById(userId);
        if (userToDelete.isPresent()) {
            userRepository.deleteById(userId);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully.");
            redirectAttributes.addFlashAttribute("messageCategory", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "User not found.");
            redirectAttributes.addFlashAttribute("messageCategory", "danger");
        }

        return "redirect:/delete_user";
    }

    // ✅ Extracted isAdmin check
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return true;

        return userRepository.findByUsername(authentication.getName())
                .map(user -> "ADMIN".equals(user.getRole().name()))
                .orElse(false);
    }
}package org.library.controller;

import lombok.extern.slf4j.Slf4j;
import org.library.entity.User;
import org.library.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/delete_user")
public class DeleteUserController {

    private final UserRepository userRepository;

    public DeleteUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Show delete user page
    @GetMapping
    public String showDeleteUserPage(Authentication authentication, Model model,
                                     RedirectAttributes redirectAttributes) {

        if (isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("message", "Access denied.");
            redirectAttributes.addFlashAttribute("messageCategory", "danger");
            return "redirect:/dashboard";
        }

        List<User> allUsers = userRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        return "delete_user";
    }

    // ✅ Handle delete user form submission
    @PostMapping
    public String handleDeleteUser(@RequestParam("user_id") Long userId,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        if (isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("message", "Access denied.");
            redirectAttributes.addFlashAttribute("messageCategory", "danger");
            return "redirect:/dashboard";
        }

        Optional<User> userToDelete = userRepository.findById(userId);
        if (userToDelete.isPresent()) {
            userRepository.deleteById(userId);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully.");
            redirectAttributes.addFlashAttribute("messageCategory", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "User not found.");
            redirectAttributes.addFlashAttribute("messageCategory", "danger");
        }

        return "redirect:/delete_user";
    }

    // ✅ Extracted isAdmin check
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return true;

        return userRepository.findByUsername(authentication.getName())
                .map(user -> "ADMIN".equals(user.getRole().name()))
                .orElse(false);
    }
}
