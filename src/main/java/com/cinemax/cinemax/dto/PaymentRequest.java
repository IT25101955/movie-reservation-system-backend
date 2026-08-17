package com.cinemax.cinemax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Carry payment request values between application layers
public class PaymentRequest {

    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;


    @NotBlank(message = "Card number is required")
    @Pattern(
        regexp = "\\d{16}",
        message = "Card number must contain exactly 16 digits"
    )
    private String cardNumber;


    @NotBlank(message = "Expiry date is required")
    @Pattern(
        regexp = "(0[1-9]|1[0-2])/\\d{2}",
        message = "Expiry date must use MM/YY format"
    )
    private String expiryDate;


    @NotBlank(message = "CVV is required")
    @Pattern(
        regexp = "\\d{3}",
        message = "CVV must contain exactly 3 digits"
    )
    private String cvv;


    public PaymentRequest() {
    }


    // Getters read field values; setters update them
    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(
            String cardholderName) {

        this.cardholderName =
                cardholderName;
    }


    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(
            String cardNumber) {

        this.cardNumber =
                cardNumber;
    }


    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(
            String expiryDate) {

        this.expiryDate =
                expiryDate;
    }


    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
