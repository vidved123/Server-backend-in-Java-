package org.library.controller;

import lombok.extern.slf4j.Slf4j;
import org.library.dto.Dashboarddto;
import org.library.entity.User;
import org.library.repository.UserRepository;
import org.library.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getDashboardView(Authentication authentication, Model model,
                                   @ModelAttribute("message") String message,
                                   @ModelAttribute("messageCategory") String category) {

        String username = authentication.getName();
        log.debug("📩 Dashboard requested by user: {}", username);

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.warn("❌ User not found: {}", username);
            return "error/404";
        }

        User user = optionalUser.get();
        Dashboarddto dashboardData = dashboardService.getDashboardDataForUser(user);

        model.addAttribute("data", dashboardData);
        model.addAttribute("userRole", user.getRole().name());

        // Only load admin-related data if user is ADMIN
        if ("ADMIN".equalsIgnoreCase(user.getRole().name())) {
            model.addAttribute("user", new User()); // For Add User form
            List<User> allUsers = userRepository.findAll();
            model.addAttribute("allUsers", allUsers);
        }

        if (StringUtils.hasText(message)) {
            model.addAttribute("message", message);
            model.addAttribute("messageCategory", category);
        }

        return "dashboard";
    }
}
