package com.cinemax.cinemax.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Map this class to a database table
@Entity
@Table(name = "users")
public class User {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String fullName;

    // =====================================================
    // CUSTOMER EMAIL
    // =====================================================
    //
    // Email is mandatory because CineMax uses it for:
    //
    // - Customer login
    // - Booking notifications
    // - Cancellation/refund notifications
    // - Admin announcements/promotions
    //
    // @NotBlank prevents empty email submissions.
    // @Email checks the basic email format.
    // The database also prevents NULL and duplicate emails.
    //
    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    @Size(
            max = 150,
            message = "Email address cannot exceed 150 characters"
    )
    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String phone;

    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;


    public User() {
    }


    public User(
            String fullName,
            String email,
            String password,
            String phone,
            Role role) {

        this.fullName = fullName;

        // Normalize email before storing it.
        setEmail(email);

        this.password = password;
        this.phone = phone;
        this.role = role;
        this.enabled = true;
    }


    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getEmail() {
        return email;
    }

    // Store emails in a consistent format.
    //
    // Example:
    //
    // "  Student@GMAIL.COM  "
    //
    // becomes:
    //
    // "student@gmail.com"
    //
    // This helps prevent accidental duplicate accounts
    // caused by spaces or different capital letters.
    public void setEmail(String email) {

        if (email == null) {

            this.email = null;

        } else {

            this.email =
                    email.trim()
                         .toLowerCase();
        }
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
