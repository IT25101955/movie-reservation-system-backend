package com.cinemax.cinemax.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Allow Spring to create and inject this component
@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String redirectUrl = "/movies";

        for (GrantedAuthority authority :
                authentication.getAuthorities()) {

            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;

            } else if (role.equals("ROLE_CUSTOMER")) {
                redirectUrl = "/customer/dashboard";
                break;

            } else if (role.equals("ROLE_MOVIE_MANAGER")) {
                redirectUrl = "/admin/movies";
                break;

            } else if (role.equals("ROLE_THEATRE_MANAGER")) {
                redirectUrl = "/admin/shows";
                break;
            }
        }

        response.sendRedirect(
                request.getContextPath() + redirectUrl
        );
    }
}
