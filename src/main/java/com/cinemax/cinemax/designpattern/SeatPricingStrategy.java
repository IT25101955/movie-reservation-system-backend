package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.SeatHold;
import com.cinemax.cinemax.model.ShowSchedule;

import java.math.BigDecimal;

// DESIGN PATTERN: Strategy - Supeshala - Seat Reservation and Booking.
// This interface gives one common structure for all seat price calculations.
public interface SeatPricingStrategy {

    // This method checks whether this strategy can handle the selected seat.
    boolean supports(SeatHold hold);

    // This method calculates and returns the correct price for the selected seat.
    BigDecimal calculatePrice(ShowSchedule show, SeatHold hold);
}
