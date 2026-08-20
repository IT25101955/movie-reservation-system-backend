package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.PaymentStatus;
import org.springframework.stereotype.Component;

// Spring creates this Strategy object for the payment Context.
@Component
// This concrete Strategy handles cards that are not success or pending test cards.
public class FailedCardPaymentStrategy implements PaymentGatewayStrategy {

    // These values are checked so this Strategy does not handle the other two test cards.
    private static final String SUCCESS_CARD = "4242424242424242";
    private static final String PENDING_CARD = "4000000000000000";

    // This method supports every card except the success and pending test cards.
    @Override
    public boolean supports(String cardNumber) {
        // Check that the card is not the success test card.
        boolean isNotSuccessCard = !SUCCESS_CARD.equals(cardNumber);

        // Check that the card is not the pending test card.
        boolean isNotPendingCard = !PENDING_CARD.equals(cardNumber);

        // Return true only when both checks are true.
        return isNotSuccessCard && isNotPendingCard;
    }

    // This method returns FAILED for the supported cards.
    @Override
    public PaymentStatus process(String cardNumber) {
        // Return the failed payment status.
        return PaymentStatus.FAILED;
    }
}
