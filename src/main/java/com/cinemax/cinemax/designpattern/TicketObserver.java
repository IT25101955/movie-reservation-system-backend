package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Ticket;

// DESIGN PATTERN: Observer - Minupama - Ticket and Notification Management.
// This is the Observer interface for objects that want ticket updates.
public interface TicketObserver {

    // This method is called when a new ticket is generated.
    void update(Ticket ticket);
}
