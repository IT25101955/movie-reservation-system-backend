package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.PaymentStatus;
import org.springframework.stereotype.Component;

// Spring creates this Strategy object for the payment Context.
@Component
// This concrete Strategy handles the test card that should succeed.
public class SuccessfulCardPaymentStrategy implements PaymentGatewayStrategy {

    // This is the test card number used to simulate a successful payment.
    private static final String SUCCESS_CARD = "4242424242424242";

    // This method checks whether the entered card is the success test card.
    @Override
    public boolean supports(String cardNumber) {
        // Return true only when the card number matches the success card.
        return SUCCESS_CARD.equals(cardNumber);
    }

    // This method returns SUCCESS for the supported card.
    @Override
    public PaymentStatus process(String cardNumber) {
        // Return the successful payment status.
        return PaymentStatus.SUCCESS;
    }
}
