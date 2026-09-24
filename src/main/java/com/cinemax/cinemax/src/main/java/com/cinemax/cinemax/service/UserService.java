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


    // Hash the password and register an enabled customer account
    public User registerCustomer(User user) {

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        user.setRole(Role.CUSTOMER);

        user.setEnabled(true);

        return userRepository.save(user);
    }


    // Check whether the email address is already registered
    public boolean emailExists(String email) {

        return userRepository.existsByEmail(email);
    }

    // Search the database and report when the record is missing
    public User getUserByEmail(String email) {

        // Optional holds the user when the database finds a match
        Optional<User> user = userRepository.findByEmail(email);

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
