package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.SeatCategory;
import com.cinemax.cinemax.model.SeatHold;
import com.cinemax.cinemax.model.ShowSchedule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Spring creates an object of this class so BookingService can use it.
@Component
// This concrete Strategy handles VIP seats.
public class VipSeatPricingStrategy implements SeatPricingStrategy {

    // This strategy is used only when the selected seat category is VIP.
    @Override
    public boolean supports(SeatHold hold) {
        // Check whether the seat is a VIP seat.
        return hold.getSeat().getSeatCategory() == SeatCategory.VIP;
    }

    // This method returns the VIP price stored in the show schedule.
    @Override
    public BigDecimal calculatePrice(ShowSchedule show, SeatHold hold) {
        // Return the VIP ticket price.
        return show.getVipPrice();
    }
}
