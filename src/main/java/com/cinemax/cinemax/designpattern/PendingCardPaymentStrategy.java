package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.PaymentStatus;
import org.springframework.stereotype.Component;

// Spring creates this Strategy object for the payment Context.
@Component
// This concrete Strategy handles the test card that should stay pending.
public class PendingCardPaymentStrategy implements PaymentGatewayStrategy {

    // This is the test card number used to simulate a pending payment.
    private static final String PENDING_CARD = "4000000000000000";

    // This method checks whether the entered card is the pending test card.
    @Override
    public boolean supports(String cardNumber) {
        // Return true only when the card number matches the pending card.
        return PENDING_CARD.equals(cardNumber);
    }

    // This method returns PENDING for the supported card.
    @Override
    public PaymentStatus process(String cardNumber) {
        // Return the pending payment status.
        return PaymentStatus.PENDING;
    }
}
