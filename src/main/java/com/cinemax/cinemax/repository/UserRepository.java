package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


// Database operations for User accounts
public interface UserRepository
        extends JpaRepository<User, Long> {


    // Find account using email address
    // without considering uppercase/lowercase.
    Optional<User> findByEmailIgnoreCase(
            String email
    );


    // Check whether the email has already been registered.
    boolean existsByEmailIgnoreCase(
            String email
    );


    // Search accounts by name or email.
    List<User>
    findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(

            String fullName,

            String email
    );


    // Count enabled user accounts.
    long countByEnabledTrue();


    // =================================================
    // FIND ACTIVE USERS BY ROLE
    // =================================================
    //
    // Used by NotificationService when Admin sends
    // a notification to all customers.
    //
    // Example:
    //
    // findByRoleAndEnabledTrue(Role.CUSTOMER)
    //
    List<User> findByRoleAndEnabledTrue(
            Role role
    );
}
