package com.cinemax.cinemax.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

// Spring creates this service to handle business rules
@Service
public class EmailService {

    private final JavaMailSender
            mailSender;


    // Read this setting from application configuration
    @Value("${cinemax.mail.enabled:false}")
    private boolean mailEnabled;


    // Read this setting from application configuration
    @Value("${spring.mail.username:}")
    private String fromAddress;


    // Constructor injection supplies the dependencies this class needs
    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider) {

        // Email is optional, so the application can start without a mail server
        this.mailSender =
                mailSenderProvider.getIfAvailable();
    }


    // Send an email when mail is configured and report whether it succeeded
    public boolean sendEmail(
            String to,
            String subject,
            String content) {

        if (!mailEnabled || mailSender == null) {

            return false;
        }


        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(
                fromAddress
            );

            message.setTo(to);

            message.setSubject(
                subject
            );

            message.setText(
                content
            );


            mailSender.send(
                message
            );


            return true;


        } catch (Exception e) {

            return false;
        }
    }
}
