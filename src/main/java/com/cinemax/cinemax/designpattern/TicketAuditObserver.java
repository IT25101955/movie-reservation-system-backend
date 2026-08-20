package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Ticket;
import org.springframework.stereotype.Component;

// Spring creates this second Concrete Observer object.
@Component
// This observer prints a simple audit message when a ticket is generated.
public class TicketAuditObserver implements TicketObserver {

    // This method runs automatically when the Subject sends a ticket update.
    @Override
    public void update(Ticket ticket) {
        // Print the generated ticket number in the console for demonstration.
        System.out.println("Ticket generated: " + ticket.getTicketNumber());
    }
}
