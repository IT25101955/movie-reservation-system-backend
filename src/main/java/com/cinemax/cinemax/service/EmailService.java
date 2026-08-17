package com.cinemax.cinemax.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;


// ============================================================
// EMAIL SERVICE
// ============================================================
//
// Handles sending real emails from:
//
// cinemaxg1@gmail.com
//
// Gmail credentials are NOT stored in Java code.
// They are supplied through environment variables.
//
@Service
public class EmailService {


    private final JavaMailSender mailSender;


    // Controls whether CineMax should actually send emails.
    //
    // application.properties:
    //
    // cinemax.mail.enabled=true
    @Value("${cinemax.mail.enabled:false}")
    private boolean mailEnabled;


    // Sender email address.
    //
    // This comes from:
    //
    // spring.mail.username=${CINEMAX_MAIL_USERNAME}
    @Value("${spring.mail.username:}")
    private String fromAddress;



    // ========================================================
    // CONSTRUCTOR
    // ========================================================

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider) {


        // Email is optional.
        //
        // Therefore CineMax can still run even if
        // JavaMailSender is unavailable.
        this.mailSender =
                mailSenderProvider.getIfAvailable();
    }



    // ========================================================
    // CHECK EMAIL STATUS
    // ========================================================

    public boolean isMailEnabled() {


        boolean enabled =
                mailEnabled
                && mailSender != null
                && fromAddress != null
                && !fromAddress.isBlank();


        // Helpful while testing the project.
        System.out.println(
                "CineMax email enabled: "
                + enabled
        );


        System.out.println(
                "CineMax sender: "
                + (
                    fromAddress == null
                    || fromAddress.isBlank()

                    ? "NOT CONFIGURED"

                    : fromAddress
                )
        );


        return enabled;
    }



    // ========================================================
    // SEND EMAIL
    // ========================================================

    public boolean sendEmail(

            String to,

            String subject,

            String content) {


        // ----------------------------------------------------
        // CHECK CONFIGURATION
        // ----------------------------------------------------

        if (!mailEnabled) {

            System.err.println(
                    "EMAIL NOT SENT: "
                    + "cinemax.mail.enabled is false."
            );

            return false;
        }


        if (mailSender == null) {

            System.err.println(
                    "EMAIL NOT SENT: "
                    + "JavaMailSender is unavailable."
            );

            return false;
        }


        if (fromAddress == null
                || fromAddress.isBlank()) {

            System.err.println(
                    "EMAIL NOT SENT: "
                    + "spring.mail.username is not configured."
            );

            return false;
        }


        if (to == null
                || to.isBlank()) {

            System.err.println(
                    "EMAIL NOT SENT: "
                    + "Customer email address is empty."
            );

            return false;
        }



        try {


            // ------------------------------------------------
            // CREATE EMAIL
            // ------------------------------------------------

            SimpleMailMessage message =
                    new SimpleMailMessage();


            // CineMax Gmail account
            message.setFrom(
                    fromAddress
            );


            // Customer's registered email
            message.setTo(
                    to.trim()
            );


            // Email subject
            message.setSubject(
                    subject
            );


            // Email body
            message.setText(
                    content
            );



            // ------------------------------------------------
            // SEND THROUGH GMAIL SMTP
            // ------------------------------------------------

            mailSender.send(
                    message
            );


            System.out.println(
                    "EMAIL SENT SUCCESSFULLY"
            );


            System.out.println(
                    "From: "
                    + fromAddress
            );


            System.out.println(
                    "To: "
                    + to
            );


            System.out.println(
                    "Subject: "
                    + subject
            );


            return true;


        } catch (Exception e) {


            // IMPORTANT:
            //
            // Your previous version hid this error.
            // Now we can see exactly why Gmail failed.
            System.err.println(
                    "======================================="
            );


            System.err.println(
                    "CINEMAX EMAIL SENDING FAILED"
            );


            System.err.println(
                    "From: "
                    + fromAddress
            );


            System.err.println(
                    "To: "
                    + to
            );


            System.err.println(
                    "Reason: "
                    + e.getMessage()
            );


            System.err.println(
                    "======================================="
            );


            // Print full error while developing.
            e.printStackTrace();


            return false;
        }
    }
}