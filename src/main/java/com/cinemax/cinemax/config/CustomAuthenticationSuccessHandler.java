package com.cinemax.cinemax.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;


// ============================================================
// CUSTOM LOGIN SUCCESS HANDLER
// ============================================================
//
// Controls where each user goes after successful login.
//
// IMPORTANT:
//
// For CUSTOMER accounts we first check whether Spring Security
// saved a booking page before redirecting the guest to login.
//
// Example:
//
// Guest tries:
// /booking/seats?showId=25
//
// Spring redirects:
// /login
//
// After successful login:
// /booking/seats?showId=25
//
// If there is NO saved booking request, the customer goes to:
//
// /customer/dashboard
//
@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {


    // ========================================================
    // REQUEST CACHE
    // ========================================================
    //
    // Spring Security automatically stores the URL that caused
    // an unauthenticated user to be redirected to the login page.
    //
    // We use the same session request cache to retrieve it.
    //
    private final RequestCache requestCache =
            new HttpSessionRequestCache();



    // ========================================================
    // LOGIN SUCCESS
    // ========================================================

    @Override
    public void onAuthenticationSuccess(

            HttpServletRequest request,

            HttpServletResponse response,

            Authentication authentication)

            throws IOException, ServletException {


        // ====================================================
        // CHECK USER ROLE
        // ====================================================

        for (GrantedAuthority authority :
                authentication.getAuthorities()) {


            String role =
                    authority.getAuthority();



            // =================================================
            // ADMIN
            // =================================================

            if ("ROLE_ADMIN".equals(role)) {


                // Admin always goes to Admin Dashboard.
                redirect(
                        request,
                        response,
                        "/admin/dashboard"
                );

                return;
            }



            // =================================================
            // MOVIE MANAGER
            // =================================================

            if ("ROLE_MOVIE_MANAGER".equals(role)) {


                // Movie Manager always goes to movie management.
                redirect(
                        request,
                        response,
                        "/admin/movies"
                );

                return;
            }



            // =================================================
            // THEATRE MANAGER
            // =================================================

            if ("ROLE_THEATRE_MANAGER".equals(role)) {


                // Theatre Manager always goes to show management.
                redirect(
                        request,
                        response,
                        "/admin/shows"
                );

                return;
            }



            // =================================================
            // CUSTOMER
            // =================================================

            if ("ROLE_CUSTOMER".equals(role)) {


                /*
                 Try to recover the URL that the customer
                 requested BEFORE login.

                 Example:

                 /booking/seats?showId=25
                */
                SavedRequest savedRequest =
                        requestCache.getRequest(
                                request,
                                response
                        );


                // ---------------------------------------------
                // SAVED REQUEST EXISTS
                // ---------------------------------------------

                if (savedRequest != null) {


                    String savedUrl =
                            savedRequest.getRedirectUrl();


                    /*
                     For security and good UX, we only restore
                     booking-related URLs.

                     We do NOT blindly redirect customers to any
                     saved Admin or Manager URL.
                    */
                    if (isBookingUrl(
                            savedUrl,
                            request)) {


                        // Remove it from the session so it is
                        // not reused on a future login.
                        requestCache.removeRequest(
                                request,
                                response
                        );


                        // Redirect to the exact original URL,
                        // including query parameters such as:
                        //
                        // ?showId=25
                        //
                        response.sendRedirect(
                                savedUrl
                        );

                        return;
                    }
                }


                // ---------------------------------------------
                // NORMAL CUSTOMER LOGIN
                // ---------------------------------------------
                //
                // Customer clicked Login normally,
                // not during the booking flow.
                //
                redirect(
                        request,
                        response,
                        "/customer/dashboard"
                );

                return;
            }
        }



        // ====================================================
        // FALLBACK
        // ====================================================
        //
        // If an unexpected role appears, return to movies.
        //
        redirect(
                request,
                response,
                "/movies"
        );
    }



    // ========================================================
    // CHECK WHETHER SAVED URL IS A BOOKING URL
    // ========================================================

    private boolean isBookingUrl(

            String savedUrl,

            HttpServletRequest request) {


        if (savedUrl == null
                || savedUrl.isBlank()) {

            return false;
        }


        try {


            /*
             SavedRequest usually contains a complete URL:

             http://localhost:8080/booking/seats?showId=25

             URI extracts only the path:

             /booking/seats
            */
            URI uri =
                    URI.create(
                            savedUrl
                    );


            String path =
                    uri.getPath();


            if (path == null) {

                return false;
            }


            String contextPath =
                    request.getContextPath();


            /*
             Normal Spring Boot application:

             contextPath = ""

             so expected path starts with:

             /booking/
            */
            String bookingPrefix =
                    contextPath
                            + "/booking/";


            return path.startsWith(
                    bookingPrefix
            );


        } catch (IllegalArgumentException e) {


            // Invalid URL should never stop login.
            return false;
        }
    }



    // ========================================================
    // NORMAL APPLICATION REDIRECT
    // ========================================================

    private void redirect(

            HttpServletRequest request,

            HttpServletResponse response,

            String path)

            throws IOException {


        response.sendRedirect(
                request.getContextPath()
                        + path
        );
    }
}
