package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.service.ReviewContentValidator;

// This concrete Decorator adds inappropriate-word checking to review validation.
public class ProfanityValidationDecorator extends ReviewValidationDecorator {

    // This existing service checks whether the comment contains inappropriate words.
    private final ReviewContentValidator contentValidator;

    // The constructor receives the wrapped validation and the content validator.
    public ProfanityValidationDecorator(
            ReviewValidation wrappedValidation,
            ReviewContentValidator contentValidator) {

        // Send the wrapped validation to the parent Decorator class.
        super(wrappedValidation);

        // Save the content validator so it can be used in validate().
        this.contentValidator = contentValidator;
    }

    // This method runs the previous validations and then checks inappropriate words.
    @Override
    public void validate(String comment) {
        // Run the validations that are already inside the decorator chain.
        super.validate(comment);

        // Stop when the comment contains inappropriate content.
        if (contentValidator.containsInappropriateContent(comment)) {
            throw new IllegalArgumentException(
                    "Your review contains inappropriate language. Please edit it and try again.");
        }
    }
}
