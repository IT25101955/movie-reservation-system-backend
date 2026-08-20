package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Ticket;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Spring creates this Subject object for the Observer Pattern.
@Component
// This class stores observers and informs them when a ticket is generated.
public class TicketSubject {

    // This list stores all registered ticket observers.
    private final List<TicketObserver> observers = new ArrayList<>();

    // Spring gives the two Concrete Observer objects to this constructor.
    public TicketSubject(
            TicketNotificationObserver notificationObserver,
            TicketAuditObserver auditObserver) {

        // Register the customer-notification observer.
        addObserver(notificationObserver);

        // Register the audit observer.
        addObserver(auditObserver);
    }

    // This method adds a new observer to the list.
    public void addObserver(TicketObserver observer) {
        // Add the observer so it can receive future ticket updates.
        observers.add(observer);
    }

    // This method removes an observer from the list.
    public void removeObserver(TicketObserver observer) {
        // Remove the observer so it will not receive future updates.
        observers.remove(observer);
    }

    // This method informs every registered observer about the generated ticket.
    public void notifyObservers(Ticket ticket) {
        // Go through the observers one by one.
        for (TicketObserver observer : observers) {
            // Send the generated ticket to the current observer.
            observer.update(ticket);
        }
    }
}
