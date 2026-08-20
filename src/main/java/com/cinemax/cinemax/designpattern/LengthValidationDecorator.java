package com.cinemax.cinemax.designpattern;

// This concrete Decorator adds a maximum-length rule to review validation.
public class LengthValidationDecorator extends ReviewValidationDecorator {

    // This variable stores the maximum number of allowed characters.
    private final int maximumLength;

    // The constructor receives the wrapped validation and the maximum length.
    public LengthValidationDecorator(ReviewValidation wrappedValidation, int maximumLength) {
        // Send the wrapped validation to the parent Decorator class.
        super(wrappedValidation);

        // Save the maximum allowed length.
        this.maximumLength = maximumLength;
    }

    // This method runs the previous validation and then checks the comment length.
    @Override
    public void validate(String comment) {
        // Run the validation that is already inside the decorator chain.
        super.validate(comment);

        // Stop when the review is longer than the allowed length.
        if (comment.trim().length() > maximumLength) {
            throw new IllegalArgumentException(
                    "Review cannot exceed " + maximumLength + " characters.");
        }
    }
}
