package com.cinemax.cinemax.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Define application settings managed by Spring
@Configuration
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler
            successHandler;

    // Constructor injection supplies the dependencies this class needs
    public SecurityConfig(
            CustomAuthenticationSuccessHandler successHandler) {

        this.successHandler = successHandler;
    }

    // Make the returned object available for dependency injection
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Make the returned object available for dependency injection
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Spring Security requires this builder style for URL access rules
        http
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/register",
                    "/login",
                    "/movies/**",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                .requestMatchers("/customer/**")
                .hasRole("CUSTOMER")

                .requestMatchers("/admin/movies/**")
                .hasAnyRole("ADMIN", "MOVIE_MANAGER")

                .requestMatchers(
                    "/admin/theatres/**",
                    "/admin/screens/**",
                    "/admin/shows/**"
                )
                .hasAnyRole("ADMIN","THEATRE_MANAGER")

                .requestMatchers(
                    "/booking/**",
                    "/payment/**",
                    "/tickets/**",
                    "/account/**",
                    "/reviews/**"
                )
                .hasRole("CUSTOMER")

                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                .anyRequest()
                .authenticated()
                
            )

            // Configure the CineMax login page
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll()
            )

            // Return to the public movie page after logout
            .logout(logout -> logout
                .logoutSuccessUrl("/movies?logout")
                .permitAll()
            );

        return http.build();
    }
}
