package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.List;

// Spring creates this Context and gives it all payment Strategy objects.
@Component
// This class is the Context part of the payment Strategy Pattern.
public class PaymentGatewayContext {

    // This list stores all available payment strategies.
    private final List<PaymentGatewayStrategy> strategies;

    // Spring sends all classes that implement PaymentGatewayStrategy to this constructor.
    public PaymentGatewayContext(List<PaymentGatewayStrategy> strategies) {
        // Save the strategies so they can be checked during payment.
        this.strategies = strategies;
    }

    // This method chooses the correct strategy for the entered card number.
    public PaymentStatus process(String cardNumber) {
        // Check each payment strategy one by one.
        for (PaymentGatewayStrategy strategy : strategies) {
            // Use the strategy when it supports the entered card number.
            if (strategy.supports(cardNumber)) {
                // Ask the selected strategy to process the card.
                return strategy.process(cardNumber);
            }
        }

        // Stop if no payment strategy can handle the card.
        throw new IllegalStateException("No payment strategy is available.");
    }
}
