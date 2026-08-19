package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.NotificationType;
import com.cinemax.cinemax.service.NotificationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


// ============================================================
// ADMIN NOTIFICATION CONTROLLER
// ============================================================
//
// Handles:
//
// GET  /admin/notifications
//      -> Show notification management page
//
// POST /admin/notifications/send
//      -> Send notification/broadcast
//
// POST /admin/notifications/{id}/delete
//      -> Delete one notification or complete broadcast
//
// POST /admin/notifications/clear-all
//      -> Delete all notifications
//
@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationController {


    // Service containing all notification business logic
    private final NotificationService notificationService;


    // Constructor injection
    public AdminNotificationController(
            NotificationService notificationService) {

        this.notificationService =
                notificationService;
    }


    // ============================================================
    // SHOW ADMIN NOTIFICATION PAGE
    // ============================================================

    // URL:
    //
    // http://localhost:8080/admin/notifications
    //
    @GetMapping
    public String showNotificationPage(
            Model model) {


        // Get latest notifications for the Admin table
        model.addAttribute(
                "notifications",
                notificationService
                        .getRecentNotificationsForAdmin()
        );


        // Total number of notifications
        model.addAttribute(
                "totalCount",
                notificationService
                        .getTotalNotificationsCount()
        );


        // Total number of unread notifications
        model.addAttribute(
                "unreadCount",
                notificationService
                        .getUnreadNotificationsCount()
        );


        // Tell the HTML page whether email is enabled
        model.addAttribute(
                "mailEnabled",
                notificationService
                        .isMailEnabled()
        );


        /*
         Thymeleaf template:

         src/main/resources/templates/
             admin/
                 notifications/
                     index.html
        */
        return "admin/notifications/index";
    }


    // ============================================================
    // SEND NOTIFICATION / BROADCAST
    // ============================================================

    @PostMapping("/send")
    public String sendNotification(

            // Notification title
            @RequestParam
            String title,

            // Notification message
            @RequestParam
            String message,

            // Promotion / Discount / Announcement / General
            @RequestParam(
                    defaultValue = "PROMOTION"
            )
            NotificationType type,

            /*
             Possible values:

             ALL_CUSTOMERS
             ALL_USERS
             SPECIFIC_USER
            */
            @RequestParam(
                    defaultValue = "ALL_CUSTOMERS"
            )
            String targetAudience,

            // Required only when SPECIFIC_USER is selected
            @RequestParam(
                    required = false
            )
            String specificEmail,

            // Optional promotion code
            @RequestParam(
                    required = false
            )
            String promoCode,

            // false by default because email is optional
            @RequestParam(
                    defaultValue = "false"
            )
            boolean sendEmail,

            RedirectAttributes redirectAttributes) {


        // --------------------------------------------------------
        // VALIDATE TITLE
        // --------------------------------------------------------

        if (title == null
                || title.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Notification title cannot be empty."
            );

            return "redirect:/admin/notifications";
        }


        // --------------------------------------------------------
        // VALIDATE MESSAGE
        // --------------------------------------------------------

        if (message == null
                || message.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Notification message cannot be empty."
            );

            return "redirect:/admin/notifications";
        }


        try {

            // Send the notification using NotificationService
            int recipientCount =
                    notificationService
                            .sendAdminBroadcast(

                                    title,
                                    message,
                                    type,
                                    targetAudience,
                                    specificEmail,
                                    promoCode,
                                    sendEmail
                            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",

                    "Notification sent successfully to "
                            + recipientCount
                            + " recipient(s)."
            );


        } catch (IllegalArgumentException e) {


            // Example:
            // Admin selected SPECIFIC_USER but email does not exist.
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/admin/notifications";
    }


    // ============================================================
    // DELETE NOTIFICATION
    // ============================================================

    @PostMapping("/{id}/delete")
    public String deleteNotification(

            @PathVariable
            Long id,

            /*
             false:
             Delete only this customer's copy.

             true:
             Delete the entire Admin broadcast
             from all recipients.
            */
            @RequestParam(
                    defaultValue = "false"
            )
            boolean deleteBroadcast,

            RedirectAttributes redirectAttributes) {


        try {

            int deletedCount =
                    notificationService
                            .deleteNotificationAsAdmin(

                                    id,
                                    deleteBroadcast
                            );


            // More than one record means a broadcast
            // was deleted for multiple customers.
            if (deletedCount > 1) {

                redirectAttributes.addFlashAttribute(
                        "successMessage",

                        "Broadcast deleted successfully for "
                                + deletedCount
                                + " recipients."
                );

            } else {

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Notification deleted successfully."
                );
            }


        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/admin/notifications";
    }


    // ============================================================
    // CLEAR ALL NOTIFICATIONS
    // ============================================================

    @PostMapping("/clear-all")
    public String clearAllNotifications(
            RedirectAttributes redirectAttributes) {


        // Permanently remove all notification records
        long deletedCount =
                notificationService
                        .clearAllNotificationsAsAdmin();


        redirectAttributes.addFlashAttribute(
                "successMessage",

                deletedCount
                        + " notification record(s) cleared."
        );


        return "redirect:/admin/notifications";
    }
}
