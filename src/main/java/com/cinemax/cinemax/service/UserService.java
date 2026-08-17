package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.Role;
import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // Constructor injection supplies the dependencies this class needs
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =====================================================
    // REGISTER CUSTOMER
    // =====================================================
    //
    // Creates a customer only after a valid email has
    // been supplied through the registration form.
    public User registerCustomer(
            User user) {


        // Extra safety check.
        //
        // The @NotBlank validation in User.java should
        // already catch this, but this protects the
        // service if it is called from another controller
        // in the future.
        if (user.getEmail() == null
                || user.getEmail().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Email address is required"
            );
        }


        // Normalize email again before saving.
        //
        // User.setEmail() also performs this operation,
        // but doing it here makes the business rule clear.
        user.setEmail(
                user.getEmail()
        );


        // Prevent duplicate customer accounts.
        if (emailExists(
                user.getEmail())) {

            throw new IllegalArgumentException(
                    "An account already exists with this email"
            );
        }


        // Encrypt the customer's password before
        // it is stored in the database.
        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );


        // Every account created through /register
        // is a customer account.
        user.setRole(
                Role.CUSTOMER
        );


        // Enable the account immediately.
        user.setEnabled(
                true
        );


        return userRepository.save(
                user
        );
    }


    // Check whether an email is already registered.
    public boolean emailExists(
            String email) {


        if (email == null
                || email.trim().isEmpty()) {

            return false;
        }


        return userRepository
                .existsByEmailIgnoreCase(
                        email.trim()
                );
    }

    // Search the database and report when the record is missing
    public User getUserByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        // Optional holds the user when the database finds a match
        Optional<User> user = userRepository.findByEmailIgnoreCase(email.trim());

        // Check whether the database search found a record
        if (user.isPresent()) {
            return user.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "User not found: " + email
        );
    }

    // Search customers and staff by name or email
    public List<User> getUsers(
            String keyword) {

        if (keyword == null ||
            keyword.trim().isEmpty()) {

            return userRepository.findAll();
        }

        String searchTerm = keyword.trim();

        return userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        searchTerm,
                        searchTerm
                );
    }

    // Search the database and report when the record is missing
    public User getUserById(
            Long id) {

        // Optional holds the user when the database finds a match
        Optional<User> user = userRepository.findById(id);

        // Check whether the database search found a record
        if (user.isPresent()) {
            return user.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException("User not found");
    }

    // Update the selected user's access role
    public void changeRole(
            Long userId,
            Role role) {

        User user = getUserById(userId);

        user.setRole(role);

        userRepository.save(user);
    }

    // Enable or disable the selected account
    public void toggleUserStatus(
            Long userId) {

        User user = getUserById(userId);

        if (user.isEnabled()) {
            user.setEnabled(false);
        } else {
            user.setEnabled(true);
        }

        userRepository.save(user);
    }
}
