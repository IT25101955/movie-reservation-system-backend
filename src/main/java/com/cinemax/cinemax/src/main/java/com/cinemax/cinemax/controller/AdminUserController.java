package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Role;
import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    // Constructor injection supplies the dependencies this class needs
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping
    public String users(
            @RequestParam(required = false) String keyword,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute("users", userService.getUsers(keyword));
        model.addAttribute("roles", Role.values());
        model.addAttribute("keyword", keyword);

        // Open the matching HTML template
        return "admin/users/list";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/role")
    // Update the selected user's access role
    public String changeRole(
            // Read this ID from the URL path
            @PathVariable Long id,
            @RequestParam Role role,
            // Principal identifies the currently logged-in account
            Principal principal) {

        User user = userService.getUserById(id);

        if (user.getEmail().equalsIgnoreCase(principal.getName()) &&
            role != Role.ADMIN) {
            // Ask the browser to load the next page
            return "redirect:/admin/users?selfRoleError";
        }

        userService.changeRole(id, role);

        // Ask the browser to load the next page
        return "redirect:/admin/users?roleChanged";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/toggle")
    public String toggleUser(
            // Read this ID from the URL path
            @PathVariable Long id,
            // Principal identifies the currently logged-in account
            Principal principal) {

        User user = userService.getUserById(id);

        if (user.getEmail().equalsIgnoreCase(principal.getName())) {
            // Ask the browser to load the next page
            return "redirect:/admin/users?selfDisableError";
        }

        userService.toggleUserStatus(id);

        // Ask the browser to load the next page
        return "redirect:/admin/users?statusChanged";
    }
}
