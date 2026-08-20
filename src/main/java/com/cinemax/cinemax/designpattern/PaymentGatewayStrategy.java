package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.PaymentStatus;

// DESIGN PATTERN: Strategy - Sankalpa - Payment Management.
// This interface gives one common structure for all payment gateway rules.
public interface PaymentGatewayStrategy {

    // This method checks whether this strategy can handle the card number.
    boolean supports(String cardNumber);

    // This method processes the card and returns a payment status.
    PaymentStatus process(String cardNumber);
}
