package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


// Handle browser requests and select HTML pages
@Controller
public class AuthController {

    private final UserService userService;


    // Constructor injection supplies the dependencies this class needs
    public AuthController(UserService userService) {

        this.userService = userService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        // Send these values to the HTML page
        model.addAttribute("user", new User());

        // Open the matching HTML template
        return "register";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/register")
    public String registerUser(

            @Valid @ModelAttribute("user") User user,
            BindingResult result) {


        // Only check duplicate email when the email
        // has already passed @NotBlank and @Email validation.
        if (!result.hasFieldErrors("email")
                && userService.emailExists(
                        user.getEmail())) {

            result.rejectValue(
                    "email",
                    "error.user",
                    "An account already exists with this email"
            );
        }


        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {

            // Open the matching HTML template
            return "register";
        }


        userService.registerCustomer(user);

        // Ask the browser to load the next page
        return "redirect:/login?registered";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/login")
    public String loginPage() {

        // Open the matching HTML template
        return "login";
    }
}
