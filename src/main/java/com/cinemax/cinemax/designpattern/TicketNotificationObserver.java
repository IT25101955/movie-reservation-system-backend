package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Ticket;
import com.cinemax.cinemax.service.NotificationService;
import org.springframework.stereotype.Component;

// Spring creates this Concrete Observer object.
@Component
// This observer creates a customer notification after a ticket is generated.
public class TicketNotificationObserver implements TicketObserver {

    // This existing service saves notifications for customers.
    private final NotificationService notificationService;

    // Spring gives the NotificationService to this constructor.
    public TicketNotificationObserver(NotificationService notificationService) {
        // Save the service so update() can use it.
        this.notificationService = notificationService;
    }

    // This method runs automatically when the Subject sends a ticket update.
    @Override
    public void update(Ticket ticket) {
        // Create the customer's ticket notification.
        notificationService.createTicketNotification(ticket);
    }
}
