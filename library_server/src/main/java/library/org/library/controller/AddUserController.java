package org.library.controller;

import org.library.entity.User;
import org.library.entity.enums.Role;
import org.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.annotation.Validated;

@Controller
@RequestMapping("/add_user")
public class AddUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AddUserController.class);

    public AddUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show the Add User Form (GET request)
    @GetMapping
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "add_user";  // This returns the add_user.html page (the form page)
    }

    // Handle the form submission (POST request)
    @PostMapping
    public String handleAddUser(@ModelAttribute("user") @Validated User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the errors in the form.");
            return "add_user";  // Show form again with errors
        }

        try {
            // Encrypt the password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Default to "USER" role if none is selected
            if (user.getRole() == null) {
                user.setRole(Role.USER);
            }

            // Ensure sex is not null (if not handled via validation annotations in the User entity)
            if (user.getSex() == null) {
                result.rejectValue("sex", "field.required", "Sex must be selected.");
                return "add_user";
            }

            // Save the user to the database
            userRepository.save(user);

            // Add a success message and redirect to the dashboard
            redirectAttributes.addFlashAttribute("successMessage", "User added successfully!");
            return "redirect:/dashboard";  // Redirect to dashboard after successful addition
        } catch (Exception e) {
            logger.error("❌ Error adding user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while adding the user.");
            return "redirect:/add_user";  // Return to form in case of error
        }
    }
}