package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.SeatCategory;
import com.cinemax.cinemax.model.SeatHold;
import com.cinemax.cinemax.model.ShowSchedule;
import com.cinemax.cinemax.model.TicketType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Spring creates an object of this class so BookingService can use it.
@Component
// This concrete Strategy handles normal child tickets.
public class ChildSeatPricingStrategy implements SeatPricingStrategy {

    // This strategy is used only for non-VIP child seats.
    @Override
    public boolean supports(SeatHold hold) {
        // Check that the seat is not VIP and the ticket type is CHILD.
        return hold.getSeat().getSeatCategory() != SeatCategory.VIP
                && hold.getTicketType() == TicketType.CHILD;
    }

    // This method returns the child price stored in the show schedule.
    @Override
    public BigDecimal calculatePrice(ShowSchedule show, SeatHold hold) {
        // Return the child ticket price.
        return show.getChildPrice();
    }
}
