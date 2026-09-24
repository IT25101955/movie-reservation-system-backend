package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface UserRepository extends JpaRepository<User, Long> {

    // Optional holds the user when the database finds a match
    // Find an account using its email address
    Optional<User> findByEmail(String email);

    // Check whether an account already uses this email
    boolean existsByEmail(String email);

    // Search accounts by name or email without matching letter case
    List<User>
    findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email
    );

    // Count enabled user accounts
    long countByEnabledTrue();

}
