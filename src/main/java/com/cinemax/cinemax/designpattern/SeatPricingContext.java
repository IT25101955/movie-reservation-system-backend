package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.SeatHold;
import com.cinemax.cinemax.model.ShowSchedule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

// Spring creates this Context and gives it all seat-pricing Strategy objects.
@Component
// This class is the Context part of the Strategy Pattern.
public class SeatPricingContext {

    // This list stores all available seat-pricing strategies.
    private final List<SeatPricingStrategy> strategies;

    // Spring sends all classes that implement SeatPricingStrategy to this constructor.
    public SeatPricingContext(List<SeatPricingStrategy> strategies) {
        // Save the strategies so they can be checked later.
        this.strategies = strategies;
    }

    // This method chooses the correct strategy and asks it to calculate the price.
    public BigDecimal calculatePrice(ShowSchedule show, SeatHold hold) {
        // Check each strategy one by one using a simple for loop.
        for (SeatPricingStrategy strategy : strategies) {
            // Use the first strategy that supports this seat.
            if (strategy.supports(hold)) {
                // Ask the selected strategy to calculate the price.
                return strategy.calculatePrice(show, hold);
            }
        }

        // Stop the booking if no pricing strategy can handle the seat.
        throw new IllegalArgumentException("No pricing strategy is available for this seat.");
    }
}
