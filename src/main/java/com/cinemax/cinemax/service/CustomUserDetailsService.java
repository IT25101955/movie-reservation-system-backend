package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Optional;


// Spring creates this service to handle business rules
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    // Constructor injection supplies the dependencies this class needs
    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }


    @Override
    // Load the account and its role for Spring Security login
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Search email without considering uppercase/lowercase.
        //
        // Example:
        // CUSTOMER@GMAIL.COM
        //
        // can still login to:
        //
        // customer@gmail.com
        Optional<User> optionalUser =
                userRepository
                        .findByEmailIgnoreCase(
                                email.trim()
                        );

        // Stop when the database search found no record
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(
                    "User not found with email: " + email
            );
        }

        User user = optionalUser.get();


        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}
